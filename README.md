gasp-mongo
==========

Geo-location search and storage for Gasp! project: mongoDB version


Add new geocoded location (to MongoDB collection)
-------------------------------------------------

curl -H "Accept: application/json" -H "Content-Type: application/json" -X POST http://localhost:8080/locations/new -d '{"name":"Cliff House","addressString":"1090 Point Lobos San Francisco CA 94121"}'

{"name":"Cliff House","formattedAddress":"1090 Point Lobos, San Francisco, CA 94121, USA","location":{"lat":37.7768388,"lng":-122.5120706}}


Get all locations (from MongoDB collection)
-------------------------------------------

curl -H "Accept: application/json" -X GET http://localhost:8080/locations/get

[{ "name" : "Cliff House" , "formattedAddress" : "1090 Point Lobos, San Francisco, CA 94121, USA" , "location" : { "lng" : -122.5120706 , "lat" : 37.7768388}}, { "name" : "Alices Restaurant" , "formattedAddress" : "17288 Skyline Boulevard, Woodside, CA 94062, USA" , "location" : { "lng" : -122.2649424 , "lat" : 37.3867203}}, { "name" : "Flea Street Cafe" , "formattedAddress" : "3607 Alameda De Las Pulgas, Menlo Park, CA 94025, USA" , "location" : { "lng" : -122.2011702 , "lat" : 37.4317999}}, { "name" : "The Dutch Goose" , "formattedAddress" : "3567 Alameda De Las Pulgas, Menlo Park, CA 94025, USA" , "location" : { "lng" : -122.2016498 , "lat" : 37.431867}}, { "name" : "Mikado Restaurant" , "formattedAddress" : "161 Main Street, Los Altos, CA 94022, USA" , "location" : { "lng" : -122.114929 , "lat" : 37.3793043}}, { "name" : "Sumika Grill" , "formattedAddress" : "236 Plaza Central, Los Altos, CA 94022, USA" , "location" : { "lng" : -122.1166286 , "lat" : 37.3791531}}, { "name" : "Peets Coffee" , "formattedAddress" : "367 State Street, Los Altos, CA 94022, USA" , "location" : { "lng" : -122.1179248 , "lat" : 37.3787929}}]


Geospatial query from MongoDB (centred search, radius in degrees)
--------------------------------------------------------------------------

curl -H "Accept: application/json" -H "Content-Type: application/json" -X POST http://localhost:8080/locations/geocenter -d '{"center" : {"lng" : -122.1139858 , "lat" : 37.3774655 }, "radius" : 0.005}'

[{ "name" : "Sumika Grill" , "formattedAddress" : "236 Plaza Central, Los Altos, CA 94022, USA" , "location" : { "lng" : -122.1166286 , "lat" : 37.3791531}}, { "name" : "Mikado Restaurant" , "formattedAddress" : "161 Main Street, Los Altos, CA 94022, USA" , "location" : { "lng" : -122.114929 , "lat" : 37.3793043}}, { "name" : "Peets Coffee" , "formattedAddress" : "367 State Street, Los Altos, CA 94022, USA" , "location" : { "lng" : -122.1179248 , "lat" : 37.3787929}}]



