Jetty-couchdb
=============

This jetty extension provides:

* A CouchDB based AppProvider
* Single-sign-on authenticator between jetty and CouchDB
* Jetty login module authenticating against CouchDB

See [camelback](github.com/jalpedersen/camelback) for simple example using both
jetty-couchdb and
[jetty-cluster](http://github.com/jalpedersen/jetty-cluster).


AppProvider
----------- 
`org.signaut.jetty.deploy.providers.couchdb.CouchDbAppProvider)`

This enables you to deploy apps stored in a CouchDB
database. Basically you provide it an URL to a CouchDB database and a
filter function and all matching documents with an attached war-file
and a context path will be deployed. Note that this is a very simple
way of deploying the same webapp to multiple jetty instances. (See
[jetty-cluster](http://github.com/jalpedersen/jetty-cluster) if you
need clustering of web-session data as well). Note that apps are
redeployed on document changes automatically, thanks to the excellent
change notification API in CouchDB.

Single-Sign-On
--------------
`org.signaut.jetty.server.security.authentication.CouchDbSSOAuthenticator`

Single-sign-on between CouchDB and Jetty based on the `AuthSession`
cookie set by CouchDB's HTTP authentication. In short this allows you
to do a `request.getUserPrincipal()` in your servlet and get what you
would expect. This also means that you can specify security
constraints in your `web.xml` using CouchDB roles.


It also supports normal basic HTTP authentication, where the user is
authenticated against CouchDB.

A use case for this is to allow you to access a Java webapp from a
CouchApp for instance.


Login Service
-------------
`org.signaut.jetty.server.security.CouchDbLoginService`

Jetty login service, which authenticates against CouchDB. Can be used
if one wishes to use normal Java web security like form login.





