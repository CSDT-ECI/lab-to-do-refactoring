package edu.eci.labinfo.labtodo.integration.data;

import edu.eci.labinfo.labtodo.data.TaskRepository;
import edu.eci.labinfo.labtodo.support.BaseIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;

class TaskRepositoryIT extends BaseIntegrationTest {

    @Autowired
    @SuppressWarnings("unused")
    private TaskRepository subject;

}
