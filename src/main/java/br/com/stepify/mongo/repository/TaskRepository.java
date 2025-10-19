package br.com.stepify.mongo.repository;

import br.com.stepify.mongo.entity.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {
    List<Task> findAllByDeletedFalse();
    Optional<Task> findByIdAndDeletedFalse(String id);
}
