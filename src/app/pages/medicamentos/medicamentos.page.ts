import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-medicamentos',
  templateUrl: './medicamentos.page.html',
  styleUrls: ['./medicamentos.page.scss'],
  standalone:false,
})
export class MedicamentosPage implements OnInit {
  pacienteId: number = 0;
  paciente: any;
  medicamentos: any[] = [];

  // Base de datos simulada
  personas = [
    { id:1, nombre:'Juan Pérez', edad:75, medicamentos:[
      { nombre: 'Paracetamol', dosis: '500mg', horarios: ['09:00','21:00'] },
      { nombre: 'Insulina', dosis: '10U', horarios: ['08:00','20:00'] }
    ]},
    { id:2, nombre:'María López', edad:80, medicamentos:[
      { nombre: 'Vitamina D', dosis: '1 pastilla', horarios: ['10:00'] }
    ]},
    { id:3, nombre:'Carlos Díaz', edad:70, medicamentos:[
      { nombre: 'Aspirina', dosis: '100mg', horarios: ['07:00'] }
    ]}
  ];

  constructor(private route: ActivatedRoute) { }

  ngOnInit() {
    // Obtener id del parámetro de la ruta
    this.pacienteId = Number(this.route.snapshot.paramMap.get('id'));
    // Buscar paciente
    this.paciente = this.personas.find(p => p.id === this.pacienteId);
    if(this.paciente) this.medicamentos = this.paciente.medicamentos;
  }
}
