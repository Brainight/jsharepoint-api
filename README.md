# sharepoint-api-v1


## Get user session object to pass into API for authentication
```java
        SAMLAuthenticator auth = new SAMLAuthenticator();
        SPUserSession spus = auth.login("user@mail.com", "password", "mycompany.sharepoint.com");
```

## Create empty file
```java
        SPFile file = SPApi.instance.createFile(spus, "<sharepoint-site>", "<folder/in/site>", "<filename>", true);
```

## Create file with content
```java
        FileInputStream fis = new FileInputStream("D:/sample.pdf");
        SPFile file = SPApi.instance.createFile(spus, "<sharepoint-site>", "<folder/in/site>", "<filename>", fis, true);
```

## Create Folder
```java
        SPFolder folder = SPApi.instance.createFolder(spus, "<sharepoint-site>", "<folder/in/site>", true);
```

## Delete Folder 
```java
        SPApi.instance.deleteFolder(spus, "SharepointTest", folder);
```

## Delete File
```java
        //TODO
```

## Get File Tree for Site Folder
```java
         SPFolder root = SPApi.instance.createFolder(spus, "<sharepoint-site>", "<folder/in/site>");
         //Try System.out.println(root.toString());
```


