// API Service for Googol Search Engine
// Connects to Spring Boot REST API

const API_BASE_URL = 'http://localhost:8080/api';

// ============================================================================
// Type Definitions
// ============================================================================

export interface SearchResult {
  url: string;
  title: string;
  description: string;  // Description/preview text
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
  alreadyIndexed?: boolean;
}

export interface ConnectionsResponse {
  url: string;
  connections: SearchResult[];
  totalConnections: number;
  hasConnections: boolean;
}

export interface StatisticsResponse {
  topSearches: Record<string, number>;
  averageResponseTime: Record<string, number>; // nanos
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
 * Get contextual analysis from backend AI
 * @param query - Search query
 * @param citations - Citations/short snippets from results
 * @returns Analysis string
 */
export async function getContextAnalysis(query: string, citations: string): Promise<string> {
  try {
    const response = await fetch(`${API_BASE_URL}/context-analysis`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ query, citations }),
    });

    console.log('Response from context-analysis:', response);
    if (!response.ok) {
      throw new ApiError(
        `Failed to get context analysis: ${response.statusText}`,
        response.status
      );
    }
    const data = await response.json();
    // Suporta resposta antiga (data.analysis) e nova (data.choices[0].message.content)
    if (data.analysis) return data.analysis;
    if (data.choices && Array.isArray(data.choices) && data.choices[0]?.message?.content) {
      return data.choices[0].message.content;
    }
    return '';
  } catch (error) {
    if (error instanceof ApiError) throw error;
    throw new ApiError(
      `Network error: ${error instanceof Error ? error.message : 'Unknown error'}`
    );
  }
}


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
 * @param indexAnyway - Force re-indexing if URL already exists
 * @returns Success response
 */
export async function addUrl(url: string, indexAnyway: boolean = false): Promise<AddUrlResponse> {
  try {
    const response = await fetch(`${API_BASE_URL}/barrels/add-url`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ url, indexAnyway }),
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
export async function getActiveBarrels(): Promise<string[]> {
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

    const raw = await response.json();
    const data = Array.isArray(raw) ? raw : raw?.barrels;
    return Array.isArray(data) ? data as string[] : [];
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

    const raw = await response.json();
    const data = Array.isArray(raw) ? raw : raw?.barrels;
    return Array.isArray(data) ? data as string[] : [];
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

/**
 * Get connections/backlinks for a specific URL
 * @param url - URL to find connections for
 * @param page - Page number (0-indexed)
 * @returns Connections response with list of URLs that link to the provided URL
 */
export async function getConnections(url: string, page: number = 0): Promise<ConnectionsResponse> {
  try {
    const response = await fetch(
      `${API_BASE_URL}/connections?url=${encodeURIComponent(url)}&page=${page}`,
      {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      }
    );

    if (!response.ok) {
      throw new ApiError(
        `Failed to get connections: ${response.statusText}`,
        response.status
      );
    }

    const data: ConnectionsResponse = await response.json();
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

export async function getStatistics(): Promise<StatisticsResponse> {
  try {
    const response = await fetch(`${API_BASE_URL}/statistics`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
    });
    if (!response.ok) {
      throw new ApiError(
        `Failed to get statistics: ${response.statusText}`,
        response.status
      );
    }
    const data: StatisticsResponse = await response.json();
    return data;
  } catch (error) {
    if (error instanceof ApiError) throw error;
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
    description: result.description,
    // If you have links/references data, add them here
    // links: result.references ? Array(result.references).fill('') : undefined
  };
}
