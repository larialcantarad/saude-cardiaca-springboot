package com.example.saudecardiaca.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "acompanhamentos")
public class Acompanhamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String pressaoArterial;
    private int frequenciaCardiaca;
    private int nivelOxigenacao;
    private double pesoCorporal;
    private String sintomas;
    private LocalDateTime dataRegistro;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @PrePersist
    protected void onCreate() {
        this.dataRegistro = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPressaoArterial() { return pressaoArterial; }
    public void setPressaoArterial(String pressaoArterial) { this.pressaoArterial = pressaoArterial; }

    public int getFrequenciaCardiaca() { return frequenciaCardiaca; }
    public void setFrequenciaCardiaca(int frequenciaCardiaca) { this.frequenciaCardiaca = frequenciaCardiaca; }

    public int getNivelOxigenacao() { return nivelOxigenacao; }
    public void setNivelOxigenacao(int nivelOxigenacao) { this.nivelOxigenacao = nivelOxigenacao; }

    public double getPesoCorporal() { return pesoCorporal; }
    public void setPesoCorporal(double pesoCorporal) { this.pesoCorporal = pesoCorporal; }

    public String getSintomas() { return sintomas; }
    public void setSintomas(String sintomas) { this.sintomas = sintomas; }

    public LocalDateTime getDataRegistro() { return dataRegistro; }
    public void setDataRegistro(LocalDateTime dataRegistro) { this.dataRegistro = dataRegistro; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}