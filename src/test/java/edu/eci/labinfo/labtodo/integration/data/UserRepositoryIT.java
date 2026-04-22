package edu.eci.labinfo.labtodo.integration.data;

import edu.eci.labinfo.labtodo.data.UserRepository;
import edu.eci.labinfo.labtodo.support.BaseIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;

class UserRepositoryIT extends BaseIntegrationTest {

    @Autowired
    @SuppressWarnings("unused")
    private UserRepository subject;

}
