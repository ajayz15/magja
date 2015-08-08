# Manual Download #

Download the jar file [here](http://code.google.com/p/magja/downloads/list) and put in your classpath.

# Automatic Installation through Maven #

Put the sonatype repository on your pom.xml like this:

```
<repositories>
...
        <repository>
            <id>sonatype</id>
            <name>Sonatype Repository</name>
            <url>http://oss.sonatype.org/content/groups/public</url>
        </repository>
...
</repositories>
```

Then, also put the dependency on your pom.xml:

```

<dependencies>
...
    <dependency>
      <groupId>com.google.code.magja</groupId>
      <artifactId>magja</artifactId>
      <version>0.0.3</version>
    </dependency>
...
</dependencies>

```

The artifact will be downloaded automatically to your project.

### Create the Magento Connection Properties File ###

Create the file /src/main/resources/magento-api.properties (or in your default classpath) with your proper data to magento installation, for example:

```
# connection to magento parameters
magento-api-username=yourMagentoApiUser
magento-api-password=youtMagentoApiPassword
magento-api-url=http://localhost/magento/index.php/api/soap/

# the ID of the default attribute set in magento
default-attribute-set-id=4

# the ID of the default root category in magento
default-root-category-id=2
```


### Extend Magento Catalog Api ###

The Magento Core API doesn't have methods to manipulate some features wich are required for Magja. So you have to install a Magento Module to provide that's features, all you have to do is download the Magja Magento Extension Module from http://code.google.com/p/magja/downloads/detail?name=MagjaCatalogExt.zip

```
# download package
wget http://magja.googlecode.com/files/MagjaCatalogExt.zip -O /tmp/MagjaCatalogExt.zip

# extract to magento directory (if necessary change /var/www/ to your magento dir)
unzip /tmp/MagjaCatalogExt.zip -d /var/www/

# change file owner to webserver user
chown www-data:www-data /var/www/app/code/community/Magja -R
```