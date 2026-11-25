package com.railway.trainservice.repository;



import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.railway.trainservice.entity.TrainFare;

@Repository
public interface TrainFareRepository extends JpaRepository<TrainFare, Long> {

    List<TrainFare> findByTrain_Id(Long trainId);

    Optional<TrainFare> findByTrain_IdAndTravelClass(
            Long trainId,
            String travelClass
    );
}

