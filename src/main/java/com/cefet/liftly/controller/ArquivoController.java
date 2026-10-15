package com.cefet.liftly.controller;
import com.cefet.liftly.model.ArquivoUsuario;
import com.cefet.liftly.repository.ArquivoUsuarioRepository;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.util.*;
@RestController @RequestMapping("/api/arquivos")
public class ArquivoController {
 private final ArquivoUsuarioRepository repo;
 public ArquivoController(ArquivoUsuarioRepository repo){this.repo=repo;}
 @PostMapping(consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
 public ResponseEntity<Map<String,String>> enviar(@RequestAttribute("usuarioAutenticadoId") String uid,@RequestParam("arquivo") MultipartFile arquivo) throws IOException {
  if(arquivo.isEmpty()||arquivo.getSize()>2_000_000) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Imagem de ate 2 MB obrigatoria");
  byte[] dados=arquivo.getBytes(); String tipo;
  if(dados.length>=3 && (dados[0]&255)==255 && (dados[1]&255)==216 && (dados[2]&255)==255) tipo="image/jpeg";
  else if(dados.length>=8 && java.util.Arrays.equals(java.util.Arrays.copyOf(dados,8),new byte[]{(byte)137,80,78,71,13,10,26,10})) tipo="image/png";
  else throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Apenas PNG ou JPEG");
  ArquivoUsuario a=new ArquivoUsuario(); a.setId(UUID.randomUUID().toString());a.setUsuarioId(uid);a.setNome("foto-perfil");a.setTipo(tipo);a.setDados(dados);repo.save(a);
  return ResponseEntity.status(201).body(Map.of("id",a.getId(),"url","/api/arquivos/"+a.getId()));
 }
 @GetMapping("/{id}") public ResponseEntity<byte[]> baixar(@PathVariable String id,@RequestAttribute("usuarioAutenticadoId") String uid){
  ArquivoUsuario a=repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  if(!a.getUsuarioId().equals(uid)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
  return ResponseEntity.ok().contentType(MediaType.parseMediaType(a.getTipo())).body(a.getDados());
 }
}
