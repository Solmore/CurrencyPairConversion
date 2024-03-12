# Currency pair conversion
This API allows customers to conversion currency. For now the resource of data this https://currate.ru/, but in future I want to change this API.
This is study project to me for learning javalin framework(https://javalin.io/). 

___
# Environments

To run this application you need to create `.env` file in root directory with next environments:
* `HOST` - host of Postgresql database
* `POSTGRES_PORT` - port of Postgresql database
* `POSTGRES_USERNAME` - username for Postgresql database
* `POSTGRES_PASSWORD` - password for Postgresql database
* `POSTGRES_DB` - name of Postgresql database
* `POSTGRES_SCHEMA` - name of Postgresql schema
* `REDIS_HOST` - host of REDIS
* `REDIS_PORT` - port of REDIS
* `KEYS` - key for using API from currate

___

# Avalible endpoints:
* `/invalidate` - refresh currency for today
* `/tickets` - get all value currency
* `/convert` - convert currency from database
* `/set` - set convert currency for yourself

___

# Future plan:
1. Change API
2. Some clearing code(change main class and maybe some other staff)

Also add Postgres json for testing.
