# Project Documentation

## Table of Contents

1. [Setup Instructions](#setup-instructions)
2. [API Endpoints](#api-endpoints)
3. [Error Handling](#error-handling)

---

## 1. Setup Instructions

### Prerequisites
Before setting up the project, ensure the following software is installed:
- **JDK 11+** (Required for running the Spring Boot application)
- **Maven** (For building the project)
- **Tomcat 9**
- **MySQL** (For database management)
- **Postman** or any API testing tool (For testing the API endpoints)

---

## 2. API Endpoints


### 2.1 Auth APIs: Accessible to all users

#### 2.1.1 **/api/auth/register-admin**
- *Method*: POST
- *Description*: Registers a new user as an admin and assigns it the ROLE_ADMIN.
- *Request Body*:
```
{
    "fullName": "batool",
    "email": "batoolbtoush98@gmail.com",
    "password": "batBAT98$",
    "dateOfBirth": "1998-10-22"
}
  ```


#### 2.1.2 **/api/auth/register-content-writer**
- *Method*: POST
- *Description*: Registers a new user as a content creater and assigns it the ROLE_CONTENT_WRITER.
- *Request Body*:
```
{
    "fullName": "batool",
    "email": "batoolbtoush98@gmail.com",
    "password": "batBAT98$",
    "dateOfBirth": "1998-10-22"
} 
```


#### 2.1.3 **/api/auth/register-normal-user**
- *Method*: POST
- *Description*: Registers a new user as a normal user and assigns it the ROLE_NORMAL.
- *Request Body*:
```
{
    "fullName": "batool",
    "email": "batoolbtoush98@gmail.com",
    "password": "batBAT98$",
    "dateOfBirth": "1998-10-22"
} 
```


#### 2.1.4 **/api/auth/login**
- *Method*: POST
- *Description*: Logs the user and generate an access (TTL: 1 minute) and a refresh (TTL: 3 hours) token
- *Request Body*:
```
{
    "email":"batoolbtoush98@gmail.com",
    "password":"batBAT98$"
}
```


#### 2.1.5 **/api/auth/generate-access-token-from-refresh-token**
- *Method*: POST
- *Description*: Generates an access token from a refresh token, after the former has expired.
- *Request Body*:
```
{
    "refresh_token":"token"
}
```



### 2.2 User APIs: Accessible only to admins

#### 2.2.1 **/api/users/create**
- *Method*: POST
- *Description*: Creates a user.
- *Request Body*:
```
    {
        "fullName": "batool",
        "email": "batoolbtoush98@gmail.com",
        "dateOfBirth": "1998-10-22",
        "password":"batBAT98$",
        "role": "ROLE_CONTENT_WRITER"
    }

```

#### 2.2.2 **/api/users/get-all**
- *Method*: GET
- *Description*: Retrieves all users.


#### 2.2.3 **/api/users/get-by-id/{id}**
- *Method*: GET
- *Description*: Retrieves a specific user based on their ID.


#### 2.2.4 **/api/users/get-by-email/{email}**
- *Method*: GET
- *Description*: Retrieves a specific user based on their email.


#### 2.2.5 **/api/users/update-by-id/{id}**
- *Method*: PUT
- *Description*: Updates a user based on their ID.
- *Request Body*:
```
{
    "fullName": "updated",
    "email": "batoolbtoush98@gmail.com",
    "role": "ROLE_ADMIN"
}
```

#### 2.2.6 **/api/users/update-by-email/{email}**
- *Method*: PUT
- *Description*: Updates a user based on their email.
- *Request Body*:
```
{
    "fullName": "updated",
    "email": "batoolbtoush98@gmail.com",
    "role": "ROLE_ADMIN"
}
```

#### 2.2.7 **/api/users/delete-by-id/{id}**
- *Method*: DELETE
- *Description*: Deletes a user based on their ID.




### 2.3 News APIs: Different access rights based on roles

#### 2.3.1 **/api/news/create**
- *Method*: POST
- *Description*: Creates a news content.
- *Access Rights*: Admins & content writers.
- *Request Body*:
```
    {
        "title": "title",
        "arabicTitle": "arabicTitle",
        "description":"description",
        "arabicDescription": "arabicDescription",
        "publishDate":"2025-02-08",
        "imageUrl":"whatever"
    }
```

#### 2.3.2 **/api/news/get-all**
- *Method*: GET
- *Description*: Retrieves all news that are active (not soft deleted).
- *Access Rights*: Admins & content writers.


#### 2.3.3 **/api/news/approve/{newsId}**
- *Method*: PUT
- *Description*: Changes the status of a new content from PENDING to APPROVED.
- *Access Rights*: Admins.


#### 2.3.4 **/api/news/get-approved**
- *Method*: GET
- *Description*: Retrieves all news that are active (not soft deleted) & APPROVED.
- *Access Rights*: Admins, content writers & normal users.


#### 2.2.5 **/api/news/delete/{newsId}**
- *Method*: DELETE
- *Description*: Deletes a news content if its status is PENDING and the user is a content writer, but if the status is APPROVED they have to get approval to the deletion request from an admin. Note: soft delete news
- *Access Rights*: Admins, content writers & normal users.


#### 2.2.6 **/api/news/get-all-deletion-requests**
- *Method*: GET
- *Description*: Retrieves all requests that have been made to delete a news content.
- *Access Rights*: Admins.


#### 2.2.7 **/api/news/process-deletion-request/{requestId}**
- *Method*: PUT
- *Description*: Allows admins to approve or reject deletion requests.
- *Access Rights*: Admins.



