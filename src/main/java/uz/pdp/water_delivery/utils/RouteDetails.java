package uz.pdp.water_delivery.utils;


public  class RouteDetails {
        private final int[] waypointOrder;
        private final long totalTime;
        private final String polyline;

        public RouteDetails(int[] waypointOrder, long totalTime, String polyline) {
            this.waypointOrder = waypointOrder;
            this.totalTime = totalTime;
            this.polyline = polyline;
        }

        public int[] getWaypointOrder() {
            return waypointOrder;
        }

        public long getTotalTime() {
            return totalTime;
        }

        public String getPolyline() {
            return polyline;
        }
    }