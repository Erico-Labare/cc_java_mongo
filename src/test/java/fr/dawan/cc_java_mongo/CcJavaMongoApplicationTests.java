package fr.dawan.cc_java_mongo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.data.mongodb.uri=mongodb://localhost:27017/cc_java_mongo_context_test",
        "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWYwMTIzNDU2Nzg5YWJjZGVm"
})
class CcJavaMongoApplicationTests {

    @Test
    void contextLoads() {
        // Vérifie que le contexte Spring démarre correctement.
    }
}
