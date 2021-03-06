/**
 * @author andre
 *
 */
package com.google.code.magja.service.product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.axis2.AxisFault;

import com.google.code.magja.magento.ResourcePath;
import com.google.code.magja.model.category.Category;
import com.google.code.magja.model.product.ConfigurableAttributeData;
import com.google.code.magja.model.product.ConfigurableProductData;
import com.google.code.magja.model.product.Product;
import com.google.code.magja.model.product.ProductAttributeSet;
import com.google.code.magja.model.product.ProductLink;
import com.google.code.magja.model.product.ProductMedia;
import com.google.code.magja.model.product.ProductType;
import com.google.code.magja.model.product.ProductTypeEnum;
import com.google.code.magja.model.product.Visibility;
import com.google.code.magja.service.GeneralServiceImpl;
import com.google.code.magja.service.ServiceException;
import com.google.code.magja.service.category.CategoryRemoteService;

public class ProductRemoteServiceImpl extends GeneralServiceImpl<Product>
		implements ProductRemoteService {

	private static final long serialVersionUID=-3943518467672208326L;

	private CategoryRemoteService categoryRemoteService;

	private ProductMediaRemoteService productMediaRemoteService;

	private ProductLinkRemoteService productLinkRemoteService;

	/*
	 * (non-Javadoc)
	 *
	 * @seecom.google.code.magja.service.product.ProductRemoteService#
	 * setCategoryRemoteService
	 * (com.google.code.magja.service.category.CategoryRemoteService)
	 */
	@Override
	public void setCategoryRemoteService(
			CategoryRemoteService categoryRemoteService) {
		this.categoryRemoteService = categoryRemoteService;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seecom.google.code.magja.service.product.ProductRemoteService#
	 * setProductMediaRemoteService
	 * (com.google.code.magja.service.product.ProductMediaRemoteService)
	 */
	@Override
	public void setProductMediaRemoteService(
			ProductMediaRemoteService productMediaRemoteService) {
		this.productMediaRemoteService = productMediaRemoteService;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seecom.google.code.magja.service.product.ProductRemoteService#
	 * setProductLinkRemoteService
	 * (com.google.code.magja.service.product.ProductLinkRemoteService)
	 */
	@Override
	public void setProductLinkRemoteService(
			ProductLinkRemoteService productLinkRemoteService) {
		this.productLinkRemoteService = productLinkRemoteService;
	}

	/**
	 * Create a object product with basic fields from the attribute map
	 *
	 * @param mpp
	 *            - the attribute map
	 * @return Product
	 */
	private Product buildProductBasic(Map<String, Object> mpp) {
		Product product = new Product();

		// populate the basic fields
		for (Map.Entry<String, Object> attribute : mpp.entrySet())
			product.set(attribute.getKey(), attribute.getValue());

		return product;
	}

	/**
	 * Build the object Product with your dependencies, for the queries
	 *
	 * @param mpp
	 * @param dependencies
	 *            - if will or not load dependencies
	 * @return Product
	 * @throws ServiceException
	 */
	private Product buildProduct(Map<String, Object> mpp, boolean dependencies)
			throws ServiceException {

		Product product = buildProductBasic(mpp);

		// product visibility
		if(mpp.get("visibility") != null) {
			Integer visi = new Integer(mpp.get("visibility").toString());
			switch (visi) {
			case 1:
				product.setVisibility(Visibility.NOT_VISIBLE_INDIVIDUALLY);
				break;
			case 2:
				product.setVisibility(Visibility.CATALOG);
				break;
			case 3:
				product.setVisibility(Visibility.SEARCH);
				break;
			case 4:
				product.setVisibility(Visibility.CATALOG_SEARCH);
				break;
			default:
				product.setVisibility(Visibility.CATALOG_SEARCH);
				break;
			}
		}

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
		if (mpp.get("category_ids") != null) {
			if (dependencies) {
				product.getCategories().addAll(
						getCategoriesBasicInfo((List<Object>) mpp
								.get("category_ids")));
			} else {
				List<Category> categories = new ArrayList<Category>();
				for (Object obj : (List<Object>) mpp.get("category_ids")) {
					Integer id = Integer.parseInt((String) obj);
					categories.add(new Category(id));
				}
				product.setCategories(categories);
			}
		}

		// Inventory
		if (dependencies) {
			Set<Product> products = new HashSet<Product>();
			products.add(product);
			getInventoryInfo(products);
		}

		// medias
		if (dependencies)
			product.setMedias(productMediaRemoteService.listByProduct(product));

		// product links
		if (dependencies)
			product.setLinks(productLinkRemoteService.list(product));

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
				if (debug)
					e.printStackTrace();
				throw new ServiceException(e.getMessage());
			}

			if (setList != null) {
				for (Map<String, Object> set : setList) {
					if (set.get("set_id").equals(set_id.toString())) {

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
			if (debug)
				e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		if (!success)
			throw new ServiceException("Not success deleting product.");

	}

	/**
	 * Delete a product by sku and category if empty
	 *
	 * @param sku
	 * @throws ServiceException
	 */
	public void deleteWithEmptyCategory(String sku) throws ServiceException {
		Product product = getBySku(sku);
		List<Category> categories = product.getCategories();

		delete(sku);

		if (categories != null) {
			for (Category category : categories) {
				categoryRemoteService.deleteEmptyRecursive(category);
			}
		}
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
			if (debug)
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
	 * com.google.code.magja.service.product.ProductRemoteService#getBySku(java.
	 * lang.String)
	 */
	@Override
	public Product getBySku(String sku) throws ServiceException {
		return getBySku(sku, true);
	}

	public Product getBySku(String sku, boolean dependencies)
			throws ServiceException {

		Map<String, Object> mpp;
		try {
			mpp = (Map<String, Object>) soapClient.call(
					ResourcePath.ProductInfo, sku);
		} catch (AxisFault e) {
			if (debug)
				e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

		if (mpp == null)
			return null;
		else
			return buildProduct(mpp, dependencies);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.google.code.magja.service.product.ProductRemoteService#getById(java
	 * .lang .Integer)
	 */
	@Override
	public Product getById(Integer id) throws ServiceException {

		Map<String, Object> mpp;
		try {
			mpp = (Map<String, Object>) soapClient.call(
					ResourcePath.ProductInfo, id);
		} catch (AxisFault e) {
			if (debug)
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
	 * @see com.google.code.magja.service.product.ProductRemoteService#listAll()
	 */
	@Override
	public List<Product> listAll() throws ServiceException {
		return list(true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.google.code.magja.service.product.ProductRemoteService#listAllNoDep()
	 */
	@Override
	public List<Product> listAllNoDep() throws ServiceException {
		return list(false);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.google.code.magja.service.product.ProductRemoteService#save(code.
	 * google .magja.model.product.Product)
	 */
	@Override
	public void save(Product product) throws ServiceException {
		save(product, "");
	}

	public void save(Product product, String storeView) throws ServiceException {
		int id = 0;
		try {
			id = getBySku(product.getSku(), false).getId();
		} catch (ServiceException e) {
			if (debug) {
				e.printStackTrace();
			}
		}

		if (id > 0) {
			// means its a existing product
			Boolean success = false;
			try {

				List<Object> newProduct = new LinkedList<Object>();
				newProduct.add(product.getSku());
				newProduct.add(product.getAllProperties());
				if (!storeView.isEmpty()) {
					newProduct.add(storeView);
				}

				success = (Boolean) soapClient.call(ResourcePath.ProductUpdate,
						newProduct);

				if (success) {
					product.setId(id);

					if (storeView.isEmpty()) {
						// FIXME: compare new and existing media instead of
						// delete and create
						for (ProductMedia media : productMediaRemoteService
								.listByProduct(product)) {
							productMediaRemoteService.delete(media);
						}
					}
				} else {
					throw new ServiceException("Error updating Product");
				}

			} catch (NumberFormatException e) {
				if (debug)
					e.printStackTrace();
				throw new ServiceException(e.getMessage());
			} catch (AxisFault e) {
				if (debug)
					e.printStackTrace();
				throw new ServiceException(e.getMessage());
			}
		} else {
			// means its a new product

			// if is a configurable product, call the proper handle
			if (product.getType().equals(ProductTypeEnum.CONFIGURABLE.getProductType()))
				handleConfigurableForNewProducts(product);

			try {

				List<Object> newProduct = (LinkedList<Object>) product
						.serializeToApi();

				id = Integer.parseInt((String) soapClient.call(
						ResourcePath.ProductCreate, newProduct));
				if (id > 0)
					product.setId(id);
				else
					throw new ServiceException("Error inserting new Product");

			} catch (NumberFormatException e) {
				if (debug)
					e.printStackTrace();
				throw new ServiceException(e.getMessage());
			} catch (AxisFault e) {
				if (debug)
					e.printStackTrace();
				throw new ServiceException(e.getMessage());
			}
		}

		// inventory
		if (product.getQty() != null)
			updateInventory(product);

		// if have media, create it too
		if (product.getMedias() != null) {

			if (!product.getMedias().isEmpty()) {
				for (ProductMedia media : product.getMedias()) {
					if (media.getImage() != null
							&& media.getImage().getData() != null)
						productMediaRemoteService.save(media);
				}
			}
		}

		// if has links, create too
		if (product.getLinks() != null) {
			if (!product.getLinks().isEmpty()) {
				for (ProductLink link : product.getLinks()) {
					if (link.getLinkType() != null
							&& (link.getId() != null || link.getSku() != null))
						productLinkRemoteService.assign(product, link);
				}
			}
		}
	}

	/*
	 * Handle configurable products just for insert new products
	 */
	private void handleConfigurableForNewProducts(Product product)
			throws ServiceException {

		// if isn't a configurable product, stop the execution
		if (!product.getType().equals(ProductTypeEnum.CONFIGURABLE.getProductType()))
			return;

		if(product.getConfigurableAttributesData() != null) {
			Map<String, Object> confAttrDataMap = new HashMap<String, Object>();

			Integer i = 0;
			for (ConfigurableAttributeData configAttr : product.getConfigurableAttributesData()) {
				confAttrDataMap.put(i.toString(), configAttr.serializeToApi());
				i++;
			}

			product.set("configurable_attributes_data", confAttrDataMap);
		}

		if (product.getConfigurableSubProducts() != null) {

			if (product.getConfigurableProductsData() == null)
				product.setConfigurableProductsData(new HashMap<String, Map<String, Object>>());

			for (ConfigurableProductData prdData : product
					.getConfigurableSubProducts()) {

				Product subprd = prdData.getProduct();

				// only save new simple products
				if (subprd.getId() == null
						&& subprd.getType().equals(ProductTypeEnum.SIMPLE.getProductType()))
					this.save(subprd);

				product.getConfigurableProductsData().put(subprd.getId().toString(),
						prdData.serializeToApi());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.google.code.magja.service.product.ProductRemoteService#
	 * listAllProductTypes ()
	 */
	@Override
	public List<ProductType> listAllProductTypes() throws ServiceException {

		List<ProductType> resultList = new ArrayList<ProductType>();

		List<Map<String, Object>> productTypes;
		try {
			productTypes = (List<Map<String, Object>>) soapClient.call(
					ResourcePath.ProductTypeList, "");
		} catch (AxisFault e) {
			if (debug)
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
	 * com.google.code.magja.service.product.ProductRemoteService#delete(java
	 * .lang .Integer)
	 */
	@Override
	public void delete(Integer id) throws ServiceException {
		delete(id, null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.google.code.magja.service.product.ProductRemoteService#delete(java
	 * .lang .String)
	 */
	@Override
	public void delete(String sku) throws ServiceException {
		delete(null, sku);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.google.code.magja.service.product.ProductRemoteService#deleteAll()
	 */
	@Override
	public void deleteAll() throws ServiceException {
		List<Product> products = listAllNoDep();
		for (Product product : products) {
			delete(product.getId());
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.google.code.magja.service.product.ProductRemoteService#getInventoryInfo
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
			if (debug)
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
						if (iv.get("is_in_stock").toString().equals("0")
								|| iv.get("is_in_stock").toString()
										.equals("false"))
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
	 * com.google.code.magja.service.product.ProductRemoteService#updateInventory
	 * (com.google.code.magja.model.product.Product)
	 */
	@Override
	public void updateInventory(Product product) throws ServiceException {

		if (product.getId() == null && product.getSku() == null)
			throw new ServiceException(
					"The product must have the id or the sku seted for update inventory");

		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("qty", product.getQty());

		if (product.getInStock() == null)
			product.setInStock(product.getQty() > 0);

		properties.put("is_in_stock", (product.getInStock() ? "1" : "0"));

		List<Object> param = new LinkedList<Object>();
		param.add((product.getId() != null ? product.getId() : product.getSku()));
		param.add(properties);

		try {
			soapClient.call(ResourcePath.ProductStockUpdate, param);
		} catch (AxisFault e) {
			if (debug)
				e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	/**
	 * Get products without category
	 *
	 * @return List<Product>
	 * @throws ServiceException
	 */
	public List<Product> getWithoutCategory() throws ServiceException {
		List<Product> withoutCategory = new ArrayList<Product>();

		List<Product> products = listAllNoDep();
		for (Product product : products) {
			if (product.getCategories().isEmpty()) {
				withoutCategory.add(product);
			}
		}

		return withoutCategory;
	}
}
