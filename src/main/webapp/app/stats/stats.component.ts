import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { StatsService } from '../services/stats.service';

@Component({
  selector: 'jhi-stats',
  templateUrl: './stats.component.html',
  styleUrls: ['./stats.component.scss'],
})
export class StatsComponent implements OnInit {
  configs: any;
  hostAgentDetails: any = {
    data: {
      memory: {},
    },
  };

  constructor(
    private statsService: StatsService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit() {
    this.statsService.getWebsiteMonitoringConfig().subscribe(data => {
      this.configs = data;
    });
    this.statsService.getHostAgentDetails().subscribe(response => {
      if (response && response.items && response.items.length > 0) {
        this.hostAgentDetails = response.items[0];
        this.cdr.detectChanges(); // Manually trigger change detection
      }
    });
  }
}
