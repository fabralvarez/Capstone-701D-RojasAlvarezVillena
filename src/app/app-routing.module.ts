import { NgModule } from '@angular/core';
import { PreloadAllModules, RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', loadChildren: () => import('./pages/login/login.module').then(m => m.LoginPageModule) },
  { path: 'registro', loadChildren: () => import('./pages/registro/registro.module').then(m => m.RegistroPageModule) },
  { path: 'inicio', loadChildren: () => import('./pages/inicio/inicio.module').then(m => m.InicioPageModule) },
  { path: 'medicamentos/:id', loadChildren: () => import('./pages/medicamentos/medicamentos.module').then(m => m.MedicamentosPageModule) },
  { path: 'alarmas', loadChildren: () => import('./pages/alarmas/alarmas.module').then(m => m.AlarmasPageModule) },
  { path: 'perfil', loadChildren: () => import('./pages/perfil/perfil.module').then(m => m.PerfilPageModule) }
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, { preloadingStrategy: PreloadAllModules })
  ],
  exports: [RouterModule]
})
export class AppRoutingModule {}
