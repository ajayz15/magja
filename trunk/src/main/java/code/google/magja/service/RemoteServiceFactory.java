/**
 *
 */
package code.google.magja.service;

import code.google.magja.service.category.CategoryRemoteService;
import code.google.magja.service.category.CategoryRemoteServiceImpl;
import code.google.magja.service.product.ProductAttributeRemoteService;
import code.google.magja.service.product.ProductAttributeRemoteServiceImpl;
import code.google.magja.service.product.ProductMediaRemoteService;
import code.google.magja.service.product.ProductMediaRemoteServiceImpl;
import code.google.magja.service.product.ProductRemoteService;
import code.google.magja.service.product.ProductRemoteServiceImpl;
import code.google.magja.soap.MagentoSoapClient;

/**
 * @author andre
 *
 */
public class RemoteServiceFactory {

	/**
	 * @return the productMediaRemoteService
	 */
	public static ProductMediaRemoteService getProductMediaRemoteService() {

		ProductMediaRemoteService productMediaRemoteService = new ProductMediaRemoteServiceImpl();
		productMediaRemoteService
				.setSoapClient(MagentoSoapClient.getInstance());

		return productMediaRemoteService;
	}

	/**
	 * @return the productAttributeRemoteService
	 */
	public static ProductAttributeRemoteService getProductAttributeRemoteService() {

		ProductAttributeRemoteService productAttributeRemoteService = new ProductAttributeRemoteServiceImpl();
		productAttributeRemoteService.setSoapClient(MagentoSoapClient
				.getInstance());

		return productAttributeRemoteService;
	}

	/**
	 * @return the productRemoteService
	 */
	public static ProductRemoteService getProductRemoteService() {

		ProductRemoteService productRemoteService = new ProductRemoteServiceImpl();
		productRemoteService.setSoapClient(MagentoSoapClient.getInstance());
		productRemoteService
				.setCategoryRemoteService(getCategoryRemoteService());
		productRemoteService
				.setProductMediaRemoteService(getProductMediaRemoteService());

		return productRemoteService;
	}

	/**
	 * @return the categoryRemoteService
	 */
	public static CategoryRemoteService getCategoryRemoteService() {

		CategoryRemoteService categoryRemoteService = new CategoryRemoteServiceImpl();
		categoryRemoteService.setSoapClient(MagentoSoapClient.getInstance());

		return categoryRemoteService;
	}

}
