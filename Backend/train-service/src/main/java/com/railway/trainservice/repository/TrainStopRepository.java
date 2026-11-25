
package com.railway.trainservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.railway.trainservice.entity.Train;
import com.railway.trainservice.entity.TrainStop;

@Repository
public interface TrainStopRepository extends JpaRepository<TrainStop, Long> {

    List<TrainStop> findByTrain_IdOrderByStopNumberAsc(Long trainId);

    @Query("""
        SELECT DISTINCT source.train
        FROM TrainStop source, TrainStop destination
        WHERE source.train.id = destination.train.id
        AND source.station.id = :sourceStationId
        AND destination.station.id = :destinationStationId
        AND source.stopNumber < destination.stopNumber
        AND source.train.active = true
    """)
    List<Train> findTrainsBetweenStations(
            @Param("sourceStationId") Long sourceStationId,
            @Param("destinationStationId") Long destinationStationId);
}
