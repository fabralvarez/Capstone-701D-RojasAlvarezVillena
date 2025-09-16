import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-registro',
  templateUrl: './registro.page.html',
  styleUrls: ['./registro.page.scss'],
  standalone: false,
})
export class RegistroPage {
  username: string = '';
  email: string = '';
  password: string = '';

  constructor(private router: Router) {}

  register() {
    if(this.username && this.email && this.password){
      alert('Registro exitoso');
      this.router.navigate(['/login']);
    } else {
      alert('Completa todos los campos');
    }
  }
}
