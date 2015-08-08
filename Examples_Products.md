# Examples for manipulate Products #

---

## Listing Products ##

### List all products with dependencies (slower) ###
```
List<Product> products = RemoteServiceFactory.getProductRemoteService().listAll();
```

### List all products without dependencies (faster) ###
```
List<Product> products = RemoteServiceFactory.getProductRemoteService().listAllNoDep();
```

---

## Get Product ##

### Get product by ID ###
```
Product product = RemoteServiceFactory.getProductRemoteService().getById(productId);
```

### Get product by SKU ###
```
Product product = RemoteServiceFactory.getProductRemoteService().getBySku(productSKU);
```

---

## Creating a New Product ##

```
Product product = new Product();
product.setSku("SKUOFPRODUCT");
product.setName("Product Name");
product.setShortDescription("This is a short description");
product.setDescription("This is a description for Product");
product.setPrice(new Double(250.99));
product.setCost(new Double(100.22));
product.setEnabled(true);
product.setWeight(new Double(0.500));
product.setType(ProductTypeEnum.SIMPLE.getProductType());
product.setAttributeSet(new ProductAttributeSet(4, "Default"));
product.setMetaDescription("one two tree");
product.setGoogleCheckout(true);

// category
List<Category> categorys = new ArrayList<Category>();
categorys.add(new Category(10));
categorys.add(new Category(20));
product.setCategories(categorys);

// websites - set the website for product
Integer[] websites = { 1 };
product.setWebsites(websites);

// inventory - set the inventory for product
product.setQty(new Double(20));
product.setInStock(true);

// you can add images for the product too

byte[] data = MagjaFileUtils.getBytesFromFileURL("http://code.google.com/images/code_sm.png");

Media image = new Media();
image.setName("google");
image.setMime("image/jpeg");
image.setData(data);

Set<ProductMedia.Type> types = new HashSet<ProductMedia.Type>();
types.add(ProductMedia.Type.IMAGE);
types.add(ProductMedia.Type.SMALL_IMAGE);

ProductMedia media = new ProductMedia();
media.setExclude(false);
media.setImage(image);
media.setLabel("Image for Product");
media.setPosition(1);
media.setTypes(types);

product.addMedia(media);

// before save, you can add linked products, like following:
ProductLink link = new ProductLink();
link.setId("ID_OR_SKU_OF_LINKED");
link.setPosition(1);
link.setQty(new Double(10));
link.setLinkType(LinkType.RELATED);

product.addLink(link);

// then, we just instanciate the service to persist the product
RemoteServiceFactory.getProductRemoteService().save(product);

// Optional: you can set the properties not mapped like the following too:
product.set("meta_description", "one two tree");
product.set("enable_googlecheckout", 1);
```