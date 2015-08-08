# Examples for create a connection to Magento host #

---

## Create connection ##

### with parameter on runtime ###
```
String user = "soap";
String pass = "test123";
String host = "http://YOUR_HOST_IP/index.php/api/soap/";

// get default connection
MagentoSoapClient magentoSoapClient = MagentoSoapClient.getInstance();

// configure connection
SoapConfig soapConfig = new SoapConfig(user, pass, host);
magentoSoapClient.setConfig(soapConfig);
```

### Create category object ###
```
// get category object
CategoryRemoteService categoryRemoteService = new CategoryRemoteServiceImpl();
categoryRemoteService.setSoapClient(magentoSoapClient);

// get details for category with id 2
Category category = categoryRemoteService.getByIdClean(2);
System.out.println(category.toString());
```


### Create category object ###
```
// get region object
RegionRemoteService regionRemoteService = new RegionRemoteServiceImpl();
regionRemoteService.setSoapClient(magentoSoapClient);

// get all regions for US
List<Region> regions = regionRemoteService.list("US");
for (Region region : regions) {
	System.out.println(region.toString());
}
```


---
