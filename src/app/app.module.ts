import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { HttpClientModule } from '@angular/common/http';

import { MaterialModule } from './material.module'
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LoginComponent } from './components/login/login.component';
import { NewOrderComponent } from './components/new-order/new-order.component';
import { MainMenuComponent } from './components/main-menu/main-menu.component';
import { TotalStockComponent } from './components/warehouse/total-stock/total-stock.component';
import { AddStockComponent } from './components/warehouse/add-stock/add-stock.component';
import { RestaurantOrdersComponent } from './components/warehouse/restaurant-orders/restaurant-orders.component';
import { WarehouseManagerComponent } from './components/warehouse/warehouse-manager/warehouse-manager.component';
import { WarehouseStockRemoveComponent } from './components/warehouse/warehouse-stock-remove/warehouse-stock-remove.component';
import { QueryStockComponent } from './components/restaurant/query-stock/query-stock.component';
import { ReceiveStockComponent } from './components/restaurant/receive-stock/receive-stock.component';
import { OrderStockComponent } from './components/restaurant/order-stock/order-stock.component';
import { RestaurantManagerComponent } from './components/restaurant/restaurant-manager/restaurant-manager.component';
import { RestaurantStockRemoveComponent } from './components/restaurant/restaurant-stock-remove/restaurant-stock-remove.component';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    NewOrderComponent,
    MainMenuComponent,
    TotalStockComponent,
    AddStockComponent,
    RestaurantOrdersComponent,
    WarehouseManagerComponent,
    WarehouseStockRemoveComponent,
    QueryStockComponent,
    ReceiveStockComponent,
    OrderStockComponent,
    RestaurantManagerComponent,
    RestaurantStockRemoveComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MaterialModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
