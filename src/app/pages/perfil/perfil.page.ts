import { Component } from '@angular/core';
import { IonicModule } from '@ionic/angular';
import { RouterModule, Router } from '@angular/router';

@Component({
  selector: 'app-perfil',
  templateUrl: './perfil.page.html',
  styleUrls: ['./perfil.page.scss'],
  standalone: false,  
})
export class PerfilPage {
  usuario = { nombre: 'Pepe gril', correo: 'Pepe@example.com' };

  constructor(private router: Router) {}

  cerrarSesion() {
    // Aquí podrías limpiar localStorage si usas sesión
    this.router.navigate(['/login']); // Redirige al login
  }
}
