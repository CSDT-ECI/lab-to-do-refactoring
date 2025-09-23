package edu.eci.labinfo.labtodo.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import edu.eci.labinfo.labtodo.model.Comment;
import edu.eci.labinfo.labtodo.model.Role;
import edu.eci.labinfo.labtodo.model.Semester;
import edu.eci.labinfo.labtodo.model.Status;
import edu.eci.labinfo.labtodo.model.Task;
import edu.eci.labinfo.labtodo.model.TypeTask;
import edu.eci.labinfo.labtodo.model.User;
import edu.eci.labinfo.labtodo.service.CommentService;
import edu.eci.labinfo.labtodo.service.PrimeFacesWrapper;
import edu.eci.labinfo.labtodo.service.SemesterService;
import edu.eci.labinfo.labtodo.service.TaskService;
import edu.eci.labinfo.labtodo.service.UserService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;

@Component
@Data
@SessionScope
public class TaskController {

    private List<Task> tasks;
    private List<Task> tasksLab;
    private List<String> selectedUsers = new ArrayList<>();
    private Task currentTask;
    private Comment comment;
    private String commentary;
    private String status;
    private String selectedSemester;

    private final TaskService taskService;
    private final UserService userService;
    private final CommentService commentService;
    private final SemesterService semesterService;
    private final PrimeFacesWrapper primeFacesWrapper;

    public TaskController(TaskService taskService, UserService userService, CommentService commentService, SemesterService semesterService, PrimeFacesWrapper primeFacesWrapper){
        this.taskService = taskService;
        this.userService = userService;
        this.commentService = commentService;
        this.semesterService = semesterService;
        this.primeFacesWrapper = primeFacesWrapper;
    }

    /**
     * Metodo que crea una nueva tarea.
     */
    public void openNew() {
        selectedUsers.clear();
        this.currentTask = new Task();
    }

    /**
     * Metodo que crea un nuevo comentario.
     */
    public void openComment() {
        this.comment = new Comment();
    }

    /**
     * Method that saves the currently selected Task object to the database.
     * If the task already exists in the database, it updates the existing task.
     * Otherwise, it creates a new task.
     * If the operation is successful, a success message is displayed to the user
     * via the FacesContext object.
     * If the operation fails, an error message is displayed.
     */
    public void saveTask() {   
        String message = "";
        List<User> selectedUsersToTask = new ArrayList<>();
        
        // Validar que solo administradores puedan crear tareas de tipo "Administradores"
        if ("Administradores".equals(this.currentTask.getTypeTask())) {
            selectedUsersToTask = userService.getUsersByRole(Role.ADMINISTRADOR.getValue());
        } else {
            // Para otros tipos de tarea
            for (String fullName : selectedUsers) {
                User user = userService.getUserByFullName(fullName);
                if (user != null) {
                    selectedUsersToTask.add(user);
                }
            }
        }
        
        if (this.currentTask.getTaskId() == null) {
            if (selectedUsers != null) {
                selectedUsers.clear();
            }
            Semester currentSemester = semesterService.getCurrentSemester();
            this.currentTask.setUsers(selectedUsersToTask);
            this.currentTask.setSemester(currentSemester);
            taskService.addTask(currentTask);
            message = "Tarea creada con éxito";
        } else {
            this.currentTask.setUsers(selectedUsersToTask);
            if (taskService.updateTask(currentTask) != null) {
                message = "Tarea actualizada con éxito";
            } else {
                message = "Error al actualizar";
            }
        }
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
        primeFacesWrapper.current().ajax().update("form:growl");
    }

    /**
     * Metodo que avisa al usuario que la tarea ha sido completada.
     */
    public void completedMessage() {
        if (this.currentTask != null) {
            Status state = Status.findByValue(this.currentTask.getStatus());
            String newState = state.next().getValue();
            if (newState.equals(Status.FINISH.getValue())) {
                currentTask.setUsers(taskService.getUsersWhoCommentedTask(currentTask.getTaskId()));
            }
            this.currentTask.setStatus(newState);
            taskService.updateTask(this.currentTask);
            String summary = "Tarea " + state.next().getValue();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, summary, null));
            primeFacesWrapper.current().ajax().update("form:growl", "form:dt-task", "form:dt-task-lab", "form:tabViewMVS", "form:task-button");

        }
    }

    public void changeLoggedTaskView() {
        primeFacesWrapper.current().ajax().update("form:growl", "form:dt-task", "form:lldt-task-lab");
    }

    /**
     * Metodo que carga las tareas de la base de datos para un usuario especifico.
     * 
     * @param userName nombre del usuario que se va a cargar sus tareas.
     */
    public void onDatabaseLoaded(String userName) {
        User user = userService.getUserByUserName(userName);
        Semester currentSemester = semesterService.getCurrentSemester();
        if (currentSemester != null) {
            this.tasks = taskService.getTaskByUserAndStatusAndSemester(user, status, currentSemester);
            this.tasksLab = taskService.getTasksByTypeAndStatusAndSemester(TypeTask.LABORATORIO.getValue(), status, currentSemester);
        }
    }

    /**
     * Metodo que carga las tareas de la base de datos cuando se va a realizar un
     * control de tareas.
     */
    public void onControlLoaded() {
        Semester currentSemester = semesterService.getCurrentSemester();
        if (this.selectedSemester != null) {
            currentSemester = semesterService.getSemesterByName(this.selectedSemester);
        }
        if (currentSemester != null) {
            this.tasks = taskService.getTasksBySemester(currentSemester);
            this.tasksLab = taskService.getTasksByTypeAndSemester(TypeTask.LABORATORIO.getValue(), currentSemester);
        }
    }

    /**
     * Metodo que hcae busqueda de las tareas por semestre de la base de datos
     * cuando se va a realizar un control de tareas.
     */
    public void onControlQuerySemester() {
        primeFacesWrapper.current().ajax().update("form:messages", "form:dt-task", "form:lldt-task-lab");
    }

    /**
     * Metodo que obtiene los comentarios de la tarea actual.
     * 
     * @return lista de comentarios de la tarea actual.
     */
    public List<Comment> getCurrentTaskComments() {
        return commentService.getComentsByTask(this.currentTask);
    }

    /**
     * Metodo que guarda un comentario en la base de datos
     * 
     * @param userName nombre del usuario que realiza el comentario
     */
    public void saveComment(String userName) {
        User userOpinion = userService.getUserByUserName(userName);
        this.comment.setDescription(commentary);
        this.comment.setTask(currentTask);
        this.comment.setCreatorUser(userOpinion);
        commentService.addComment(comment);
        commentary = "";
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Comentario agregado"));
        primeFacesWrapper.current().executeScript("PF('manageCommentDialog').hide()");
        primeFacesWrapper.current().ajax().update("form:messages", "form:comments-list");
    }

    public void loadUsers(){
        selectedUsers.clear();
        currentTask.getUsers().forEach(user -> selectedUsers.add(user.getFullName()));
    }

    /**
     * Metodo que obtiene el mensaje que se va a mostrar en el boton de la tarea
     * @param task tarea de la cual se va a obtener el mensaje
     * @return mensaje que se va a mostrar en el boton de la tarea
     */
    public String getMessageToTaskButton(Task task) {
        String message = "";
        if (task != null) {
            if (task.getStatus().equals(Status.PENDING.getValue())) {
                message = "Iniciar";
            } else if (task.getStatus().equals(Status.INPROCESS.getValue())) {
                message = "A revisión";
            } else {
                message = "Completar";
            }
        }
        return message;
    }

    /**
     * Metodo activa o desactiva el boton de la tarea dependiendo del estado de la tarea
     * @param userName nombre del usuario que esta logueado
     * @param task tarea a renderizar el boton
     * @return True si se renderiza el boton, de lo contrario False
     */
    public Boolean getRenderedToTaskButton(String userName, Task task) {
        Boolean rendered = true;
        User user = userService.getUserByUserName(userName);
        if (task != null) {
            if (task.getStatus().equals(Status.FINISH.getValue())) {
                rendered = false;
            }
            if (task.getStatus().equals(Status.REVIEW.getValue())
                    && user.getRole().equals(Role.MONITOR.getValue())) {
                rendered = false;
            }
        }
        return rendered;
    }
    
}
