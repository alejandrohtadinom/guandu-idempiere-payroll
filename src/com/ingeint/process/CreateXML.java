package com.ingeint.process;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.compiere.model.MCurrency;
import org.compiere.model.MOrg;
import org.compiere.model.MOrgInfo;
import org.compiere.model.MSysConfig;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.eevolution.model.MHRMovement;
import org.eevolution.model.MHRProcess;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.ingeint.base.CustomProcess;

public class CreateXML extends CustomProcess {
	
	int p_HR_Concept_ID = 0;
	Timestamp ValidFrom = null;
	Timestamp ValidTo = null;
	Timestamp DateDoc = null;
    Integer p_AD_Org_ID = 0;
	
	protected void prepare() {
		for (ProcessInfoParameter para : getParameter()) {
			String name = para.getParameterName();
			if (name.equals(MOrg.COLUMNNAME_AD_Org_ID))
				p_AD_Org_ID = para.getParameterAsInt();
			else if (name.equals("DateDoc"))
				DateDoc = para.getParameterAsTimestamp();
			else if (name.equals("HR_Concept_ID"))
				p_HR_Concept_ID = para.getParameterAsInt();
			else if (name.equals("ValidFrom"))
				ValidFrom = para.getParameterAsTimestamp();
			else if ( name.equals("ValidTo"))
				ValidTo = para.getParameterAsTimestamp();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
	}

	@Override
	protected String doIt() throws Exception {
		MOrg org = new MOrg(getCtx(), p_AD_Org_ID, get_TrxName());
		MOrgInfo orgInfo = MOrgInfo.get(p_AD_Org_ID);
		String period = DateDoc.toString().substring(0, 8).replace("-","");
		// Root Element
		Element root = new Element("RelacionRetencionesISLR");
		root.setAttribute("RifAgente", orgInfo.getTaxID());
		root.setAttribute("Periodo", period);
		
		List<MHRMovement> movements = new Query(getCtx(), MHRMovement.Table_Name,
				"HR_Movement.ValidFrom >= ? And HR_Movement.ValidTo <= ? AND HR_Movement.HR_Concept_ID = ? AND HR_Movement.AD_Client_ID = ? AND HR_Movement.AD_Org_ID = ? ", get_TrxName())
						.setParameters(new Object[] { ValidFrom, ValidTo, p_HR_Concept_ID, getAD_Client_ID(), p_AD_Org_ID })
					    .addJoinClause("JOIN C_BPartner bp on bp.C_BPartner_ID = HR_Movement.C_BPartner_ID ")
						.setOrderBy("BP.TaxID")
						.list();

		for (MHRMovement move : movements) {
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
			String date = format.format(new Date(move.getHR_Process().getHR_Period().getEndDate().getTime()));
			int PerecentageRet = MSysConfig.getIntValue("PerecentageRet", 0, move.getAD_Client_ID());
			String sql = "SELECT Amount "
					+ "FROM HR_Movement "
					+ "WHERE HR_Concept_ID = ? "
					+ "AND HR_Process_ID = ? "
					+ "AND C_BPartner_ID = ?";
			int precisionCu = MCurrency.getStdPrecision(getCtx(),Env.getContextAsInt(getCtx(), "$C_Currency_ID"));	
			BigDecimal AmountFactor = DB.getSQLValueBD(null, sql, new Object[] {PerecentageRet, move.getHR_Process_ID(), move.getC_BPartner_ID()});	
			
			// Element 1
			if(move.getC_BPartner().getTaxID().length() > 8) {
			
				Element detail = new Element("DetalleRetencion");
				detail.addContent(new Element("RifRetenido").addContent("V" + move.getC_BPartner().getTaxID()));
				detail.addContent(new Element("NumeroFactura").addContent("0"));
				detail.addContent(new Element("NumeroControl").addContent("NA"));
				detail.addContent(new Element("FechaOperacion").addContent(date));
				detail.addContent(new Element("CodigoConcepto").addContent("001"));
				detail.addContent(new Element("MontoOperacion").addContent(move.getAmount().setScale(precisionCu, RoundingMode.HALF_UP).toString()));
				detail.addContent(new Element("PorcentajeRetencion").addContent(AmountFactor.setScale(precisionCu, RoundingMode.HALF_UP).toString()));
				root.addContent(detail);};
	
			}
		
		Document doc = new Document();
		doc.setRootElement(root);

		// Create the XML
		XMLOutputter outter = new XMLOutputter();
		Format format = Format.getPrettyFormat();
	    format.setEncoding("ISO-8859-1");
		outter.setFormat(format);		
		File file = new File(org.getName() + "_" + period + ".xml");
		outter.output(doc, new FileWriter(file));

		processUI.download(file);
		return "Creado: " + file;
	}

}
