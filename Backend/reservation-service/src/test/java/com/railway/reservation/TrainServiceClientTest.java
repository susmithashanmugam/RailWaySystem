package com.railway.reservation;


import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.railway.reservation.client.TrainServiceClient;

class TrainServiceClientTest {

    @Test
    void shouldHaveFeignClientAnnotation() {

        FeignClient annotation =
                TrainServiceClient.class.getAnnotation(FeignClient.class);

        assertNotNull(annotation);
        assertEquals("train-service", annotation.name());
    }

    @Test
    void shouldHaveGetTrainByIdMapping() throws Exception {

        Method method = TrainServiceClient.class.getMethod(
                "getTrainById", Long.class);

        GetMapping mapping = method.getAnnotation(GetMapping.class);

        assertNotNull(mapping);
        assertArrayEquals(
                new String[]{"/api/trains/id/{trainId}"},
                mapping.value()
        );

        Parameter parameter = method.getParameters()[0];

        PathVariable pathVariable =
                parameter.getAnnotation(PathVariable.class);

        assertNotNull(pathVariable);
        assertEquals("trainId", pathVariable.value());
    }

    @Test
    void shouldHaveGetTrainRouteMapping() throws Exception {

        Method method = TrainServiceClient.class.getMethod(
                "getTrainRoute", Long.class);

        GetMapping mapping = method.getAnnotation(GetMapping.class);

        assertNotNull(mapping);
        assertArrayEquals(
                new String[]{"/api/trains/{trainId}/route"},
                mapping.value()
        );

        assertTrue(List.class.isAssignableFrom(method.getReturnType()));

        PathVariable pathVariable =
                method.getParameters()[0]
                        .getAnnotation(PathVariable.class);

        assertNotNull(pathVariable);
        assertEquals("trainId", pathVariable.value());
    }

    @Test
    void shouldHaveGetTrainFaresMapping() throws Exception {

        Method method = TrainServiceClient.class.getMethod(
                "getTrainFares", Long.class);

        GetMapping mapping = method.getAnnotation(GetMapping.class);

        assertNotNull(mapping);
        assertArrayEquals(
                new String[]{"/api/trains/{trainId}/fares"},
                mapping.value()
        );

        assertTrue(List.class.isAssignableFrom(method.getReturnType()));

        PathVariable pathVariable =
                method.getParameters()[0]
                        .getAnnotation(PathVariable.class);

        assertNotNull(pathVariable);
        assertEquals("trainId", pathVariable.value());
    }
}
