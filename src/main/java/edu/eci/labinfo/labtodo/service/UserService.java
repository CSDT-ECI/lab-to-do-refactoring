package edu.eci.labinfo.labtodo.service;

import java.util.List;
import java.util.Arrays;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.eci.labinfo.labtodo.data.UserRepository;
import edu.eci.labinfo.labtodo.model.User;
import edu.eci.labinfo.labtodo.model.AccountType;
import edu.eci.labinfo.labtodo.model.Role;
import edu.eci.labinfo.labtodo.model.Task;
import edu.eci.labinfo.labtodo.model.LabToDoExeption;
import java.time.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TaskService taskService;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, TaskService taskService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.taskService = taskService;
    }

    public User addUser(User user) throws LabToDoExeption {
        if (!userRepository.existsByUserName(user.getUserName())) {
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
            user.setCreationDate(LocalDateTime.now());
            user.setUpdateDate(LocalDateTime.now());
            user.setLastLoginDate(LocalDateTime.now());
        } else {
            throw new LabToDoExeption(LabToDoExeption.USER_NAME_EXISTS);
        }

        return userRepository.save(user);
    }

    public User getUserByUserName(String username) {
        if (userRepository.findByUserName(username).isPresent()) {
            return userRepository.findByUserName(username).get();
        }
        return null;
    }

    public User getUserByFullName(String fullName) {
        if (userRepository.findByFullName(fullName).isPresent()) {
            return userRepository.findByFullName(fullName).get();
        }
        return null;
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    /**
     * Obtiene usuarios por rol excluyendo aquellos con accountType "inactivo" o "sin verificar"
     * @param role el rol de los usuarios a obtener
     * @return lista de usuarios activos con el rol especificado
     */
    public List<User> getActiveUsersByRole(String role) {
        List<String> excludedAccountTypes = Arrays.asList(
            AccountType.INACTIVO.getValue(),
            AccountType.SIN_VERIFICAR.getValue()
        );
        return userRepository.findByRoleAndAccountTypeNotIn(role, excludedAccountTypes);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public User updateUser(User user) {
        if (userRepository.existsById(user.getUserId())) {
            // Obtener el usuario actual de la base de datos para comparar cambios
            User currentUser = userRepository.findById(user.getUserId()).orElse(null);
            
            if (currentUser != null) {
                // Verificar si cambió el rol a administrador o el accountType a un estado válido
                boolean roleChangedToAdmin = !Role.ADMINISTRADOR.getValue().equals(currentUser.getRole()) 
                                           && Role.ADMINISTRADOR.getValue().equals(user.getRole());
                
                boolean accountTypeChangedToValid = !shouldReceiveAdminTasks(currentUser) 
                                                   && shouldReceiveAdminTasks(user);
                
                // Actualizar fecha de modificación
                user.setUpdateDate(LocalDateTime.now());
                
                // Guardar el usuario actualizado
                User updatedUser = userRepository.save(user);
                
                // Si el rol cambió a administrador o el accountType cambió a uno válido,
                // asignar las tareas de administrador
                if (roleChangedToAdmin || accountTypeChangedToValid) {
                    assignAdminTasksToUser(updatedUser);
                }
                
                return updatedUser;
            }
            
            return userRepository.save(user);
        }
        return null;
    }

    public void deleteUser(String userName) {
        userRepository.delete(getUserByUserName(userName));
    }

    public void deleteAllUsers() {
        userRepository.deleteAll();
    }

    /**
     * Verifica si un usuario debe recibir tareas de administrador automáticamente
     * @param user el usuario a verificar
     * @return true si debe recibir las tareas, false en caso contrario
     */
    private boolean shouldReceiveAdminTasks(User user) {
        // Debe ser administrador
        if (!Role.ADMINISTRADOR.getValue().equals(user.getRole())) {
            return false;
        }
        
        // Debe tener un accountType válido (no inactivo ni sin verificar)
        String accountType = user.getAccountType();
        return !AccountType.INACTIVO.getValue().equals(accountType) && 
               !AccountType.SIN_VERIFICAR.getValue().equals(accountType);
    }

    /**
     * Asigna automáticamente las tareas de administrador no finalizadas a un usuario
     * @param user el usuario al que se le asignarán las tareas
     */
    private void assignAdminTasksToUser(User user) {
        if (shouldReceiveAdminTasks(user)) {
            List<Task> adminTasks = taskService.getNonFinishedAdminTasks();
            
            for (Task task : adminTasks) {
                // Verificar si el usuario ya está asignado a la tarea
                boolean alreadyAssigned = task.getUsers().stream()
                    .anyMatch(u -> u.getUserId().equals(user.getUserId()));
                
                if (!alreadyAssigned) {
                    task.getUsers().add(user);
                    taskService.updateTask(task);
                }
            }
        }
    }

}
