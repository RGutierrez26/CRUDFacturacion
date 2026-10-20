

//consumo la api que quiera
export async function getResources<T>(url: string): Promise<T | any> {
    const response = await fetch(url);
    return await response.json();
}