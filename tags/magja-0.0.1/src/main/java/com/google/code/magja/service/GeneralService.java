/**
 *
 */
package com.google.code.magja.service;

import java.io.Serializable;

import com.google.code.magja.model.BaseMagentoModel;
import com.google.code.magja.soap.MagentoSoapClient;

/**
 * @author andre
 *
 */
public interface GeneralService<T extends BaseMagentoModel> extends Serializable {

	public abstract void setSoapClient(MagentoSoapClient soapClient);

}
