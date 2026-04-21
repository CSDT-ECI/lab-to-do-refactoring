package edu.eci.labinfo.labtodo.integration.data;

import edu.eci.labinfo.labtodo.data.CommentRepository;
import edu.eci.labinfo.labtodo.support.BaseIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;

class CommentRepositoryIT extends BaseIntegrationTest {

    @Autowired
    @SuppressWarnings("unused")
    private CommentRepository subject;

}
