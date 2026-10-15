package com.cefet.liftly.controller;

import com.cefet.liftly.dto.ExercicioCatalogoResponse;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/exercicios-catalogo")
public class ExercicioCatalogoController {
    private static final List<ExercicioCatalogoResponse> CATALOGO = List.of(
            new ExercicioCatalogoResponse("e01", "Supino Reto", "Peito", "Exercício básico para peitoral maior", "Barra"),
            new ExercicioCatalogoResponse("e02", "Supino Inclinado", "Peito", "Foco na parte superior do peitoral", "Barra"),
            new ExercicioCatalogoResponse("e03", "Supino Declinado", "Peito", "Foco na parte inferior do peitoral", "Barra"),
            new ExercicioCatalogoResponse("e04", "Crucifixo", "Peito", "Isolamento do peitoral", "Halteres"),
            new ExercicioCatalogoResponse("e05", "Crossover", "Peito", "Finalização do peitoral no cabo", "Cabo"),
            new ExercicioCatalogoResponse("e06", "Peck Deck", "Peito", "Isolamento do peitoral na máquina", "Máquina"),
            new ExercicioCatalogoResponse("e07", "Puxada Frontal", "Costas", "Latíssimo do dorso com pulley", "Cabo"),
            new ExercicioCatalogoResponse("e08", "Remada Curvada", "Costas", "Espessura das costas com barra", "Barra"),
            new ExercicioCatalogoResponse("e09", "Remada Unilateral", "Costas", "Remada com halter em apoio", "Halteres"),
            new ExercicioCatalogoResponse("e10", "Levantamento Terra", "Costas", "Exercício composto para costas e posterior", "Barra"),
            new ExercicioCatalogoResponse("e11", "Pull-up", "Costas", "Barra fixa com peso corporal", "Peso Corporal"),
            new ExercicioCatalogoResponse("e12", "Serrote", "Costas", "Remada unilateral apoiado no banco", "Halteres"),
            new ExercicioCatalogoResponse("e13", "Desenvolvimento com Barra", "Ombros", "Press militar para deltóide", "Barra"),
            new ExercicioCatalogoResponse("e14", "Desenvolvimento com Halteres", "Ombros", "Press para deltóide com halteres", "Halteres"),
            new ExercicioCatalogoResponse("e15", "Elevação Lateral", "Ombros", "Isolamento do deltóide lateral", "Halteres"),
            new ExercicioCatalogoResponse("e16", "Elevação Frontal", "Ombros", "Isolamento do deltóide anterior", "Halteres"),
            new ExercicioCatalogoResponse("e17", "Remada Alta", "Ombros", "Deltóide e trapézio com barra", "Barra"),
            new ExercicioCatalogoResponse("e18", "Rosca Direta", "Bíceps", "Exercício básico para bíceps", "Barra"),
            new ExercicioCatalogoResponse("e19", "Rosca Alternada", "Bíceps", "Rosca com halteres alternando os braços", "Halteres"),
            new ExercicioCatalogoResponse("e20", "Rosca Martelo", "Bíceps", "Ênfase no braquial e braquiorradial", "Halteres"),
            new ExercicioCatalogoResponse("e21", "Rosca Scott", "Bíceps", "Isolamento do bíceps no banco Scott", "Barra"),
            new ExercicioCatalogoResponse("e22", "Rosca Concentrada", "Bíceps", "Pico do bíceps sentado", "Halteres"),
            new ExercicioCatalogoResponse("e23", "Tríceps Pulley", "Tríceps", "Extensão de tríceps no cabo", "Cabo"),
            new ExercicioCatalogoResponse("e24", "Tríceps Francês", "Tríceps", "Extensão acima da cabeça", "Halteres"),
            new ExercicioCatalogoResponse("e25", "Tríceps Testa", "Tríceps", "Extensão de tríceps deitado", "Barra"),
            new ExercicioCatalogoResponse("e26", "Mergulho", "Tríceps", "Flexão entre bancos ou paralelas", "Peso Corporal"),
            new ExercicioCatalogoResponse("e27", "Kickback", "Tríceps", "Extensão de tríceps inclinado", "Halteres"),
            new ExercicioCatalogoResponse("e28", "Agachamento Livre", "Pernas", "Exercício rainha para quadríceps e glúteos", "Barra"),
            new ExercicioCatalogoResponse("e29", "Leg Press", "Pernas", "Quadríceps e glúteos na máquina", "Máquina"),
            new ExercicioCatalogoResponse("e30", "Extensora", "Pernas", "Isolamento do quadríceps", "Máquina"),
            new ExercicioCatalogoResponse("e31", "Flexora", "Pernas", "Isolamento do bíceps femoral", "Máquina"),
            new ExercicioCatalogoResponse("e32", "Stiff", "Pernas", "Posterior de coxa com barra", "Barra"),
            new ExercicioCatalogoResponse("e33", "Avanço", "Pernas", "Lunges para quadríceps e glúteos", "Halteres"),
            new ExercicioCatalogoResponse("e34", "Hack Squat", "Pernas", "Agachamento na máquina hack", "Máquina"),
            new ExercicioCatalogoResponse("e35", "Hip Thrust", "Glúteos", "Empurrada de quadril com barra", "Barra"),
            new ExercicioCatalogoResponse("e36", "Glúteo no Cabo", "Glúteos", "Isolamento do glúteo no cabo", "Cabo"),
            new ExercicioCatalogoResponse("e37", "Abdução no Cabo", "Glúteos", "Abdução de quadril no cabo", "Cabo"),
            new ExercicioCatalogoResponse("e38", "Abdominal Supra", "Abdômen", "Contração do reto abdominal", "Peso Corporal"),
            new ExercicioCatalogoResponse("e39", "Abdominal Infra", "Abdômen", "Elevação de pernas para abdômen inferior", "Peso Corporal"),
            new ExercicioCatalogoResponse("e40", "Prancha", "Abdômen", "Estabilização do core isometricamente", "Peso Corporal"),
            new ExercicioCatalogoResponse("e41", "Abdominal no Cabo", "Abdômen", "Crunch no pulley", "Cabo"),
            new ExercicioCatalogoResponse("e42", "Panturrilha em Pé", "Panturrilha", "Elevação de calcanhar em pé", "Máquina"),
            new ExercicioCatalogoResponse("e43", "Panturrilha Sentado", "Panturrilha", "Elevação de calcanhar sentado", "Máquina"),
            new ExercicioCatalogoResponse("e44", "Rosca de Pulso", "Antebraços", "Flexão de punho para antebraço", "Barra"),
            new ExercicioCatalogoResponse("e45", "Rosca de Punho Reversa", "Antebraços", "Extensão de punho para braquiorradial", "Barra")
    );

    @GetMapping
    public List<ExercicioCatalogoResponse> listar(@RequestParam(required = false) String grupo,
                                                  @RequestParam(required = false) String termo) {
        return CATALOGO.stream()
                .filter(e -> grupo == null || e.grupoMuscular().equalsIgnoreCase(grupo))
                .filter(e -> termo == null || termo.isBlank()
                        || e.nome().toLowerCase().contains(termo.toLowerCase())
                        || e.grupoMuscular().toLowerCase().contains(termo.toLowerCase()))
                .toList();
    }

    @GetMapping("/grupos")
    public List<String> listarGrupos() {
        return CATALOGO.stream().map(ExercicioCatalogoResponse::grupoMuscular).distinct().toList();
    }
}
