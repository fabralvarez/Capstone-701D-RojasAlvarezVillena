import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.page.html',
  styleUrls: ['./login.page.scss'],
  standalone: false,
})
export class LoginPage {
  username: string = '';
  password: string = '';

  constructor(private router: Router) {}

  login() {
    if(this.username && this.password){
      this.router.navigate(['/inicio']);
    } else {
      alert("Ingresa usuario y contraseña");
    }
  }

  goRegistro() {
    this.router.navigate(['/registro']);
  }
}
