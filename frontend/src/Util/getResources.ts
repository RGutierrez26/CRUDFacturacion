// Consumo genérico de API REST
export async function getResources<T>(url: string): Promise<T> {
    const response = await fetch(url);
    if (!response.ok) {
        const errorData = await response.json().catch(() => null);
        const mensaje = errorData?.message || errorData?.error || `Error HTTP ${response.status}: ${response.statusText}`;
        throw new Error(mensaje);
    }
    return await response.json();
}
