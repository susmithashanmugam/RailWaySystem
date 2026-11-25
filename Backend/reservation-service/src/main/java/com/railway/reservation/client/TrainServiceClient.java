
package com.railway.reservation.client;

import com.railway.reservation.dto.TrainResponse;
import com.railway.reservation.dto.TrainStopResponse;
import com.railway.reservation.dto.TrainFareResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "train-service")
public interface TrainServiceClient {

    @GetMapping("/api/trains/id/{trainId}")
    TrainResponse getTrainById(
            @PathVariable("trainId") Long trainId
    );

    @GetMapping("/api/trains/{trainId}/route")
    List<TrainStopResponse> getTrainRoute(
            @PathVariable("trainId") Long trainId
    );

    @GetMapping("/api/trains/{trainId}/fares")
    List<TrainFareResponse> getTrainFares(
            @PathVariable("trainId") Long trainId
    );
}
