/**
 * orderMapConfig.js
 * Configuration and global state management for order map
 */

// ============================================
// GLOBAL STATE
// ============================================

var OrderMapState = {
    // Parsed server data
    orders: [],
    currentOrders: [],
    couriers: [],
    company: {},
    currentCourier: {},

    // Map instance
    map: null,

    // Placemarks storage
    placemarks: [],

    // Company coordinates
    companyCoords: [],

    // Route planning storage (in-memory)
    selectedIdsPerCourier: {},
    selectedPointsPerCourier: {},

    // Helper function to ensure courier ID is string
    asKey: function(id) {
        return String(id);
    }
};

// ============================================
// CONFIGURATION
// ============================================

var OrderMapConfig = {
    // Map settings
    map: {
        defaultZoom: 13,
        searchZoom: 17,
        controls: ['zoomControl', 'fullscreenControl']
    },

    // Icon sizes
    icons: {
        regular: {
            size: [35, 35],
            offset: [-17, -35]
        },
        selected: {
            size: [45, 45],
            offset: [-22, -45]
        },
        searched: {
            size: [50, 50],
            offset: [-25, -50]
        }
    },

    // Clickable circle radius (meters)
    clickRadius: 30,

    // Polyline styles
    polylines: {
        current: {
            color: '#0000FF',
            width: 7,
            opacity: 0.8
        },
        assigned: {
            color: '#00FF00',
            width: 7,
            opacity: 0.8
        },
        other: {
            color: 'rgba(128,128,128,0.5)',
            width: 5,
            opacity: 0.7
        }
    },

    // Order colors by status/date
    colors: {
        today: 'green',
        yesterday: 'orange',
        old: 'red',
        selected: 'violet',
        assigned: 'gray'
    },

    // Alert display duration (ms)
    alertDuration: 5000
};

// ============================================
// INITIALIZATION HELPER
// ============================================

/**
 * Initialize state with server data
 */
function initializeOrderMapState(serverData) {
    OrderMapState.orders = serverData.orders || [];
    OrderMapState.currentOrders = serverData.currentOrders || [];
    OrderMapState.couriers = serverData.couriers || [];
    OrderMapState.company = serverData.company || {};
    OrderMapState.currentCourier = serverData.currentCourier || {};

    OrderMapState.companyCoords = [
        OrderMapState.company.latitude,
        OrderMapState.company.longitude
    ];

    console.log('✅ Order Map State Initialized');
    console.log('  - Orders:', OrderMapState.orders.length);
    console.log('  - Couriers:', OrderMapState.couriers.length);
    console.log('  - Current Courier:', OrderMapState.currentCourier.id);
}