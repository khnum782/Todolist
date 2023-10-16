package br.com.gustavosouza.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gustavosouza.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {
  
  @Autowired
  private ITaskRepository taskRepository;

  @PostMapping("/")
  public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
    var id_user = request.getAttribute("idUser");
    taskModel.setIdUser((UUID) id_user);

    var currentDate = LocalDateTime.now();
    if(currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de ínicio/termino deve ser maior que a data atual");
    }

    if(taskModel.getEndAt().isBefore(taskModel.getStartAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de termino dever ser maior que a data de ínicio");
    }

    var task = this.taskRepository.save(taskModel);
    return ResponseEntity.status(HttpStatus.OK).body(task);
  }

  @GetMapping("/")
  public List<TaskModel> list(HttpServletRequest request) {
    var id_user = request.getAttribute("idUser");
    var tasks = this.taskRepository.findByIdUser((UUID) id_user);
    return tasks;
  }

  @PutMapping("/{idTask}")
  public ResponseEntity update(@RequestBody TaskModel taskModel, @PathVariable UUID idTask, HttpServletRequest request) {
    var task = this.taskRepository.findById(idTask).orElse(null);
    if(task == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
      .body("Tarefa não encontrada");
    }

    var id_user = request.getAttribute("idUser");

    if(!task.getIdUser().equals(id_user)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
      .body("Usuário não tem permissão para alterar esta tarefa");
    }

    Utils.copyNonNullProperties(taskModel, task);

    var taskUpdated = this.taskRepository.save(task);
    return ResponseEntity.ok().body(taskUpdated);
  }
}
