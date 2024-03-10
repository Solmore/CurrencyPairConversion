package org.solmore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.javalin.Javalin;
import io.javalin.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmore.config.CurrencyConfiguration;
import org.solmore.service.CurrencyService;
import org.solmore.web.controller.CurrencyController;
import org.solmore.web.mapper.CurrencyPairDtoMapper;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import me.paulschwarz.springdotenv.DotenvPropertySource;

import java.lang.reflect.Type;

@Slf4j
public class CurrencyApplication {
    private static final Logger logger = LoggerFactory.getLogger(CurrencyApplication.class);
    private static final Gson gson = new GsonBuilder().create();


    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        DotenvPropertySource.addToEnvironment(context.getEnvironment());
        context.register(CurrencyConfiguration.class);
        context.refresh();
        CurrencyService service = context.getBean("currencyService", CurrencyService.class);
        CurrencyPairDtoMapper mapper = Mappers.getMapper(CurrencyPairDtoMapper.class);


        JsonMapper gsonMapper = new JsonMapper() {
            @NotNull
            @Override
            public String toJsonString(@NotNull Object obj, @NotNull Type type) {
                return gson.toJson(obj, type);
            }
            @NotNull
            @Override
            public <T> T fromJsonString(@NotNull String json, @NotNull Type targetType) {
                return gson.fromJson(json, targetType);
            }
        };

        Javalin app = Javalin.create(config -> config.jsonMapper(gsonMapper)).start(8080);
        TaskScheduler scheduler = new TaskScheduler(service);
        scheduler.scheduleValidationPairTask();
        new CurrencyController(service, mapper).initRoutes(app);
    }
}
