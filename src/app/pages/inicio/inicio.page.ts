import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AlertController } from '@ionic/angular';
@Component({
  selector: 'app-inicio',
  templateUrl: './inicio.page.html',
  styleUrls: ['./inicio.page.scss'],
  standalone:false,
})
export class InicioPage {
  personas = [
    { nombre: 'Juan Pérez', edad: 75, id: 1 },
    { nombre: 'María López', edad: 80, id: 2 },
    { nombre: 'Carlos Díaz', edad: 70, id: 3 }
  ];

   constructor(private router: Router, private alertCtrl: AlertController) {}

  verPaciente(persona: any) {
    this.router.navigate(['/medicamentos', persona.id]);
  }

  async agregarPaciente() {
    const alert = await this.alertCtrl.create({
      header: 'Agregar Paciente',
      inputs: [
        { name: 'nombre', type: 'text', placeholder: 'Nombre completo' },
        { name: 'edad', type: 'number', placeholder: 'Edad' }
      ],
      buttons: [
        { text: 'Cancelar', role: 'cancel' },
        { 
          text: 'Agregar', 
          handler: (data) => {
            if(data.nombre && data.edad) {
              // Crear nuevo paciente con id único
              const nuevoId = this.personas.length > 0 ? Math.max(...this.personas.map(p => p.id)) + 1 : 1;
              this.personas.push({ nombre: data.nombre, edad: Number(data.edad), id: nuevoId });
            }
          }
        }
      ]
    });

    await alert.present();
  }
  irPerfil() {
    this.router.navigate(['/perfil']); // Redirige a la página del cuidador
  }
}
