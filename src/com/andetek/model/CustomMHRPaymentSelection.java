package com.andetek.model;

import com.andetek.utils.HR_BANABIH;
import com.ingeint.model.MHRPaymentSelection;
import com.ingeint.model.MPaymentSelectionType;
import java.sql.ResultSet;
import java.util.Properties;
import org.compiere.model.MDocType;
import org.compiere.model.Query;
import org.compiere.print.MPrintFormat;
import org.compiere.process.DocAction;

/**
 * Custom class for MHRPaymentSelection
 *
 * @author Alejandro H Tadino M
 *
 * TODO:
 * - Reemplazar el metodo PaymentSelection HR con un metodo que verifique si se
 * ejecuta una clase alterna para crear el reporte del banavih
 *
 */
public class CustomMHRPaymentSelection extends MHRPaymentSelection {

  /**
   *
   */
  private static final long serialVersionUID = -1314370641285329083L;

  /**
   *
   */
  public CustomMHRPaymentSelection(Properties ctx, int HR_PaymentSelection_ID,
                                   String trxName) {
    super(ctx, HR_PaymentSelection_ID, trxName);
    // TODO Auto-generated constructor stub
  }

  /**
   *
   */
  public CustomMHRPaymentSelection(Properties ctx, ResultSet rs,
                                   String trxName) {
    super(ctx, rs, trxName);
    // TODO Auto-generated constructor stub
  }

  /**
   * 	Complete Document
   *	@return new status (Complete, In Progress, Invalid, Waiting ..)
   *
   *	TODO:
   *	- Crear metodo para obtener la instancia de ING_MHRPaymentSelection
   *	- Crear metodo para exportar a un archivo los datos en CSV
   *	- Crear el reporte de Banavih
   */
  @Override
  public String completeIt() {
    super.completeIt();
    return DocAction.STATUS_Completed;
  }

  public MPaymentSelectionType t_mps = null;

  @Override
  public boolean afterSave(boolean newRecord, boolean success) {
    t_mps = (MPaymentSelectionType)getING_PaymentSelectionType();
    MDocType dt = (MDocType)this.getC_DocType();
    MPrintFormat pf = (MPrintFormat)dt.getAD_PrintFormat();
    StringBuffer err = new StringBuffer();

    if ((boolean)t_mps.get_Value("Testing_Class_Call")) {
      HR_BANABIH banavih = new HR_BANABIH();
      banavih.exportToFile(pf, this, err);
    }
    return super.afterSave(newRecord, success);
  }
}
