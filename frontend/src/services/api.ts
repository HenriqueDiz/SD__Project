// API Service for Googol Search Engine
// Connects to Spring Boot REST API

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

// ============================================================================
// Type Definitions
// ============================================================================

export interface SearchResult {
  url: string;
  title: string;
  snippet: string;      // Description/preview text
  references: number;   // Number of backlinks
}

export interface SearchResponse {
  query: string;
  results: SearchResult[];
  totalResults: number;
  currentPage: number;
  pageSize: number;
  totalPages: number;
  hasResults: boolean;
}

export interface BarrelInfo {
  name: string;
  host: string;
  port: number;
  indexSize: number;
}

export interface HealthStatus {
  status: 'UP' | 'DOWN';
  gateway: 'CONNECTED' | 'DISCONNECTED';
  timestamp: string;
}

export interface SystemInfo {
  appName: string;
  version: string;
  gatewayHost: string;
  gatewayPort: number;
}

export interface AddUrlRequest {
  url: string;
}

export interface AddUrlResponse {
  success: boolean;
  message: string;
  url: string;
}

// ============================================================================
// API Error Handling
// ============================================================================

export class ApiError extends Error {
  constructor(
    message: string,
    public status?: number,
    public data?: any
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

// ============================================================================
// API Service Functions
// ============================================================================

/**
 * Search for pages containing the query terms
 * @param query - Search query (single or multiple words)
 * @param page - Page number (0-indexed)
 * @returns Search results
 */
export async function searchQuery(
  query: string,
  page: number = 0
): Promise<SearchResponse> {
  try {
    const response = await fetch(
      `${API_BASE_URL}/search?q=${encodeURIComponent(query)}&page=${page}`,
      {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      }
    );

    if (!response.ok) {
      throw new ApiError(
        `Search failed: ${response.statusText}`,
        response.status
      );
    }

    const data: SearchResponse = await response.json();
    return data;
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }
    throw new ApiError(
      `Network error: ${error instanceof Error ? error.message : 'Unknown error'}`
    );
  }
}

/**
 * Add a URL to the indexing queue
 * @param url - URL to index
 * @returns Success response
 */
export async function addUrl(url: string): Promise<AddUrlResponse> {
  try {
    const response = await fetch(`${API_BASE_URL}/barrels/add-url`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ url }),
    });

    if (!response.ok) {
      throw new ApiError(
        `Failed to add URL: ${response.statusText}`,
        response.status
      );
    }

    const data: AddUrlResponse = await response.json();
    return data;
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }
    throw new ApiError(
      `Network error: ${error instanceof Error ? error.message : 'Unknown error'}`
    );
  }
}

/**
 * Get list of active barrels
 * @returns List of active barrels
 */
export async function getActiveBarrels(): Promise<BarrelInfo[]> {
  try {
    const response = await fetch(`${API_BASE_URL}/barrels/active`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      throw new ApiError(
        `Failed to get active barrels: ${response.statusText}`,
        response.status
      );
    }

    const data: BarrelInfo[] = await response.json();
    return data;
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }
    throw new ApiError(
      `Network error: ${error instanceof Error ? error.message : 'Unknown error'}`
    );
  }
}

/**
 * Get list of all registered barrels
 * @returns List of registered barrels
 */
export async function getRegisteredBarrels(): Promise<string[]> {
  try {
    const response = await fetch(`${API_BASE_URL}/barrels/registered`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      throw new ApiError(
        `Failed to get registered barrels: ${response.statusText}`,
        response.status
      );
    }

    const data: string[] = await response.json();
    return data;
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }
    throw new ApiError(
      `Network error: ${error instanceof Error ? error.message : 'Unknown error'}`
    );
  }
}

/**
 * Check API and Gateway health status
 * @returns Health status
 */
export async function checkHealth(): Promise<HealthStatus> {
  try {
    const response = await fetch(`${API_BASE_URL}/health`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      throw new ApiError(
        `Health check failed: ${response.statusText}`,
        response.status
      );
    }

    const data: HealthStatus = await response.json();
    return data;
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }
    throw new ApiError(
      `Network error: ${error instanceof Error ? error.message : 'Unknown error'}`
    );
  }
}

/**
 * Get system information
 * @returns System info
 */
export async function getSystemInfo(): Promise<SystemInfo> {
  try {
    const response = await fetch(`${API_BASE_URL}/info`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      throw new ApiError(
        `Failed to get system info: ${response.statusText}`,
        response.status
      );
    }

    const data: SystemInfo = await response.json();
    return data;
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }
    throw new ApiError(
      `Network error: ${error instanceof Error ? error.message : 'Unknown error'}`
    );
  }
}

// ============================================================================
// Utility Functions
// ============================================================================

/**
 * Check if API is reachable
 * @returns true if API is reachable, false otherwise
 */
export async function isApiAvailable(): Promise<boolean> {
  try {
    const health = await checkHealth();
    return health.status === 'UP';
  } catch {
    return false;
  }
}

/**
 * Format search result for display
 * @param result - Search result from API
 * @returns Formatted result for AnimatedList component
 */
export function formatSearchResultForDisplay(result: SearchResult) {
  return {
    url: result.url,
    title: result.title,
    description: result.snippet,
    // If you have links/references data, add them here
    // links: result.references ? Array(result.references).fill('') : undefined
  };
}
