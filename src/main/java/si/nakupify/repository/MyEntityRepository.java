package si.nakupify.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import si.nakupify.entity.MyEntity;

@ApplicationScoped
public class MyEntityRepository implements PanacheRepositoryBase<MyEntity, Long> {
}
