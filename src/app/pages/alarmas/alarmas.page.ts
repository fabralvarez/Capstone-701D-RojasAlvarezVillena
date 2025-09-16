import { Component } from '@angular/core';

@Component({
  selector: 'app-alarmas',
  templateUrl: './alarmas.page.html',
  styleUrls: ['./alarmas.page.scss'],
  standalone: false,
})
export class AlarmasPage {
  alarmas = [
    { medicamento: 'Paracetamol', hora: '09:00', activo: true },
    { medicamento: 'Insulina', hora: '12:00', activo: false }
  ];
}
