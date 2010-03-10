/**
 *
 */
package code.google.magja.service.product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.axis2.AxisFault;

import code.google.magja.magento.ResourcePath;
import code.google.magja.model.category.Category;
import code.google.magja.model.product.Product;
import code.google.magja.model.product.ProductAttributeSet;
import code.google.magja.model.product.ProductType;
import code.google.magja.model.product.ProductTypeEnum;
import code.google.magja.service.GeneralServiceImpl;
import code.google.magja.service.ServiceException;
import code.google.magja.service.category.CategoryRemoteService;

/**
 * @author andre
 *
 */
@SuppressWarnings("serial")
public class ProductRemoteServiceImpl extends GeneralServiceImpl<Product>
		implements ProductRemoteService {

	private CategoryRemoteService categoryRemoteService;

	/*
	 * (non-Javadoc)
	 *
	 * @seecode.google.magja.service.product.ProductRemoteService#
	 * setCategoryRemoteService
	 * (code.google.magja.service.category.CategoryRemoteService)
	 */
	@Override
	public void setCategoryRemoteService(
			CategoryRemoteService categoryRemoteService) {
		this.categoryRemoteService = categoryRemoteService;
	}

	private Product buildProductBasic(Map<String, Object> mpp) {
		Product product = new Product();

		// populate the basic fields
		for (Map.Entry<String, Object> attribute : mpp.entrySet())
			product.set(attribute.getKey(), attribute.getValue());

		return product;
	}

	/*
	 * Build the object Product with your dependencies
	 */
	private Product buildProduct(Map<String, Object> mpp, boolean dependencies)
			throws ServiceException {

		Product product = buildProductBasic(mpp);

		// set product type
		if (mpp.get("type") != null) {

			ProductType type = ProductTypeEnum.getTypeOf((String) mpp
					.get("type"));

			if (type == null && dependencies) {
				/*
				 * means its a type not covered by the enum, so we have to look
				 * in magento api to get this type
				 */
				List<ProductType> types = listAllProductTypes();
				for (ProductType productType : types) {
					if (productType.getType().equals((String) mpp.get("type"))) {
						type = productType;
						break;
					}
				}
			}

			if (type != null)
				product.setType(type);
		}

		// set the attributeSet
		if (mpp.get("set") != null && dependencies)
			product.setAttributeSet(getAttributeSet((String) mpp.get("set")));

		// categories - dont get the full tree, only basic info of categories
		if (mpp.get("category_ids") != null && dependencies)
			product.getCategories().addAll(
					getCategoriesBasicInfo((List<Object>) mpp
							.get("category_ids")));

		// Inventory
		if (dependencies) {
			Set<Product> products = new HashSet<Product>();
			products.add(product);
			getInventoryInfo(products);
		}

		return product;
	}

	/**
	 * @param ids
	 * @return list of categories with specified ids, just the basic info
	 * @throws ServiceException
	 */
	private List<Category> getCategoriesBasicInfo(List<Object> ids)
			throws ServiceException {
		List<Category> categories = new ArrayList<Category>();
		for (Object obj : ids) {
			Integer id = Integer.parseInt((String) obj);
			Category category = categoryRemoteService.getByIdClean(id);
			categories.add(category);
		}
		return categories;
	}

	/**
	 * @param id
	 * @return the ProductAttributeSet with the specified id
	 * @throws ServiceException
	 */
	private ProductAttributeSet getAttributeSet(String id)
			throws ServiceException {

		ProductAttributeSet prdAttSet = new ProductAttributeSet();
		Integer set_id = Integer.parseInt(id);

		// if are the default attribute set, that not list on the api, so we
		// have to set manually
		if (set_id.equals(soapClient.getConfig().getDefaultAttributeSetId())) {

			prdAttSet.setId(set_id);
			prdAttSet.setName("Default");

		} else {
			List<Map<String, Object>> setList;
			try {
				setList = (List<Map<String, Object>>) soapClient.call(
						ResourcePath.ProductAttributeSetList, "");
			} catch (AxisFault e) {
				e.printStackTrace();
				throw new ServiceException(e.getMessage());
			}

			if (setList != null) {
				for (Map<String, Object> set : setList) {
					if (set.get("set_id").equals(set_id)) {

						for (Map.Entry<String, Object> att : set.entrySet())
							prdAttSet.set(att.getKey(), att.getValue());

						break;
					}
				}
			}
		}

		return prdAttSet;
	}

	/**
	 * Delete a product by your id (prefered) or your sku
	 *
	 * @param id
	 * @param sku
	 * @throws ServiceException
	 */
	private void delete(Integer id, String sku) throws ServiceException {

		Boolean success = false;
		try {
			if (id != null) {
				success = (Boolean) soapClient.call(ResourcePath.ProductDelete,
						id);
			} else if (sku != null) {
				success = (Boolean) soapClient.call(ResourcePath.ProductDelete,
						sku);
			}

		} catch (AxisFault e) {
			System.out.println(e.getMessage());
			throw new ServiceException(e.getMessage());
		}
		if (!success)
			throw new ServiceException("Not success deleting product.");

	}

	/**
	 * List the products, if dependencies is true, the products will be
	 * populated with all your dependencies, otherwise, no.
	 *
	 * @param dependencies
	 * @return List<Product>
	 * @throws ServiceException
	 */
	private List<Product> list(boolean dependencies) throws ServiceException {
		List<Product> products = new ArrayList<Product>();

		List<Map<String, Object>> productList;

		try {
			productList = (List<Map<String, Object>>) soapClient.call(
					ResourcePath.ProductList, "");
		} catch (AxisFault e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

		if (productList == null)
			return products;

		for (Map<String, Object> mpp : productList)
			products.add(buildProduct(mpp, dependencies));

		return products;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * code.google.magja.service.product.ProductRemoteService#getBySku(java.
	 * lang.String)
	 */
	@Override
	public Product getBySku(String sku) throws ServiceException {

		Map<String, Object> mpp;
		try {
			mpp = (Map<String, Object>) soapClient.call(
					ResourcePath.ProductInfo, sku);
		} catch (AxisFault e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

		if (mpp == null)
			return null;
		else
			return buildProduct(mpp, true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * code.google.magja.service.product.ProductRemoteService#getById(java.lang
	 * .Integer)
	 */
	@Override
	public Product getById(Integer id) throws ServiceException {

		Map<String, Object> mpp;
		try {
			mpp = (Map<String, Object>) soapClient.call(
					ResourcePath.ProductInfo, id);
		} catch (AxisFault e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

		if (mpp == null)
			return null;
		else
			return buildProduct(mpp, true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see code.google.magja.service.product.ProductRemoteService#listAll()
	 */
	@Override
	public List<Product> listAll() throws ServiceException {
		return list(true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * code.google.magja.service.product.ProductRemoteService#listAllNoDep()
	 */
	@Override
	public List<Product> listAllNoDep() throws ServiceException {
		return list(false);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * code.google.magja.service.product.ProductRemoteService#save(code.google
	 * .magja.model.product.Product)
	 */
	@Override
	public void save(Product product) throws ServiceException {

		if (product.getId() == null) {

			// means its a new product
			try {

				List<Object> newProduct = (LinkedList<Object>) product
						.serializeToApi();

				Integer id = Integer.parseInt((String) soapClient.call(
						ResourcePath.ProductCreate, newProduct));
				if (id > -1)
					product.setId(id);
				else
					throw new ServiceException("Error inserting new Product");

			} catch (NumberFormatException e) {
				e.printStackTrace();
				throw new ServiceException(e.getMessage());
			} catch (AxisFault e) {
				e.printStackTrace();
				throw new ServiceException(e.getMessage());
			}

			// inventory
			if(product.getQty() != null && (new Double(0) < product.getQty()))
				updateInventory(product);

		} else {
			// TODO implement the update product
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * code.google.magja.service.product.ProductRemoteService#listAllProductTypes
	 * ()
	 */
	@Override
	public List<ProductType> listAllProductTypes() throws ServiceException {

		List<ProductType> resultList = new ArrayList<ProductType>();

		List<Map<String, Object>> productTypes;
		try {
			productTypes = (List<Map<String, Object>>) soapClient.call(
					ResourcePath.ProductTypeList, "");
		} catch (AxisFault e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

		if (productTypes == null)
			return resultList;

		for (Map<String, Object> type : productTypes) {
			ProductType productType = new ProductType();
			for (Map.Entry<String, Object> attribute : type.entrySet())
				productType.set(attribute.getKey(), attribute.getValue());
			resultList.add(productType);
		}

		return resultList;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * code.google.magja.service.product.ProductRemoteService#delete(java.lang
	 * .Integer)
	 */
	@Override
	public void delete(Integer id) throws ServiceException {
		delete(id, null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * code.google.magja.service.product.ProductRemoteService#delete(java.lang
	 * .String)
	 */
	@Override
	public void delete(String sku) throws ServiceException {
		delete(null, sku);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * code.google.magja.service.product.ProductRemoteService#getInventoryInfo
	 * (java.util.Set)
	 */
	@Override
	public void getInventoryInfo(Set<Product> products) throws ServiceException {

		String[] productIds = new String[products.size()];
		int i = 0;
		for (Product product : products)
			productIds[i++] = product.getId().toString();

		List<Object> param = new LinkedList<Object>();
		param.add(productIds);

		List<Map<String, Object>> resultList = null;
		try {
			resultList = (List<Map<String, Object>>) soapClient.call(
					ResourcePath.ProductStockList, param);
		} catch (AxisFault e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

		for (Map<String, Object> iv : resultList) {
			for (Product product : products) {
				if (product.getId().equals(
						Integer.parseInt((String) iv.get("product_id")))) {
					if (iv.get("qty") != null || !"".equals(iv.get("qty")))
						product.setQty(Double.parseDouble((String) iv
								.get("qty")));
					if (iv.get("is_in_stock") != null
							|| !"".equals(iv.get("is_in_stock"))) {
						if (((String) iv.get("is_in_stock")).equals("0"))
							product.setInStock(false);
						else
							product.setInStock(true);
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * code.google.magja.service.product.ProductRemoteService#updateInventory
	 * (code.google.magja.model.product.Product)
	 */
	@Override
	public void updateInventory(Product product) throws ServiceException {

		if (product.getId() == null && product.getSku() == null)
			throw new ServiceException(
					"The product must have the id or the sku seted for update inventory");

		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("qty", product.getQty());

		if(product.getInStock() == null)
			product.setInStock(product.getQty() > 0);

		properties.put("is_in_stock", (product.getInStock() ? "1" : "0"));

		List<Object> param = new LinkedList<Object>();
		param
				.add((product.getId() != null ? product.getId() : product
						.getSku()));
		param.add(properties);

		try {
			soapClient.call(ResourcePath.ProductStockUpdate, param);
		} catch (AxisFault e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}
}
