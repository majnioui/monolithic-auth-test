import { Component, OnInit } from '@angular/core';
import { StatsService } from '../services/stats.service';

@Component({
  selector: 'jhi-stats',
  templateUrl: './stats.component.html',
})
export class StatsComponent implements OnInit {
  configs: any;

  constructor(private statsService: StatsService) {}

  ngOnInit() {
    this.statsService.getWebsiteMonitoringConfig().subscribe(data => {
      this.configs = data;
    });
  }
}
