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
  installedSoftware: any;
  infrastructureTopology: any;
  eventsData: any[] = [];

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
        this.cdr.detectChanges();
      }
    });

    // Fetch installed software versions
    this.statsService.getInstalledSoftware().subscribe(
      data => {
        this.installedSoftware = data;
        this.cdr.detectChanges();
      },
      error => {
        console.error('Failed to fetch installed software:', error);
      },
    );

    // Fetch infrastructure topology
    this.statsService.getInfrastructureTopology().subscribe(
      data => {
        this.infrastructureTopology = data.nodes.map((node: any) => ({
          plugin: node.plugin,
          label: node.label,
          pluginId: node.entityId.pluginId,
        }));
        this.cdr.detectChanges();
      },
      error => {
        console.error('Failed to fetch infra topology:', error);
      },
    );
    // Fetch all events
    this.statsService.getAllEvents().subscribe(
      data => {
        this.eventsData = data;
        this.cdr.detectChanges();
      },
      error => {
        console.error('Failed to fetch all events:', error);
      },
    );
  }
}
