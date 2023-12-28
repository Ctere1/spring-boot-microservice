# Run
For running docker:
`docker-compose up`

After changes need to maven clean install so just run:
`bash service_install.bash`

```bash
eureka.instance.hostname=api-gateway
eureka.instance.instance-id=${spring.application.name}
eureka.client.service-url.defaultZone=http://discovery-server:8761/eureka/
```

> [!WARNING]     
> These are required fields for registering with Eureka in Docker

> [!IMPORTANT]      
> It is necessary to add `127.0.0.1 keycloak` to the host file. (C:\Windows\System32\drivers\etc)