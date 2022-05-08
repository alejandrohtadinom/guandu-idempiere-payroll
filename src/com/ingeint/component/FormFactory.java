/**
 * 
 */
package com.ingeint.component;

import org.eevolution.form.HRActionNoticeForm;

import com.ingeint.base.CustomFormFactory;

import ve.net.dcs.form.WConceptTest;

/**
 * @author <a href="mailto:victor.suarez.is@gmail.com">Ing. Victor Suarez</a>
 *
 */
public class FormFactory extends CustomFormFactory {

	@Override
	protected void initialize() {
		registerForm(HRActionNoticeForm.class);
		registerForm(WConceptTest.class);
	}

}
