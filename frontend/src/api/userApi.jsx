import axios from "axios"
import { jwtDecode } from "jwt-decode";

export const getAllUsers = async(token) => {

    

   
        if (!token) return;
    
        const decoded = jwtDecode(token);
        
        


        
    
    const res = await axios.post(
  "http://a36b26e9e7919469ba1660e86fa0a9b4-399270085.ap-south-1.elb.amazonaws.com:8080/api",
  form, {
        headers : {
            Authorization : `Bearer ${token}`
        }
    });
    
    const filtered = res.data.filter((user) => user.email !== decoded.sub);

    return filtered;
}