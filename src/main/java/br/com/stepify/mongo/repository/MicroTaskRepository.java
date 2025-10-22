package br.com.stepify.mongo.repository;

import br.com.stepify.mongo.entity.MicroTask;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MicroTaskRepository extends MongoRepository<MicroTask, String> {
    List<MicroTask> findAllByDeletedFalse();
    Optional<MicroTask> findByIdAndDeletedFalse(String id);
}
