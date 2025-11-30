import axios from "axios"
import { jwtDecode } from "jwt-decode";

export const getAllUsers = async(token) => {

    

   
        if (!token) return;
    
        const decoded = jwtDecode(token);
        
        


        
    
    const res = await axios.get("http://backend-userservice:8080/api", {
        headers : {
            Authorization : `Bearer ${token}`
        }
    });
    
    const filtered = res.data.filter((user) => user.email !== decoded.sub);

    return filtered;
}