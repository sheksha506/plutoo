import axios from "axios"
import { jwtDecode } from "jwt-decode";

export const getAllUsers = async(token) => {

    

   
        if (!token) return;
    
        const decoded = jwtDecode(token);
        
        


        
    
    const res = await axios.post(
  "http://k8s-default-backendu-e26db51adb-657e4e43fd19a424.elb.ap-south-1.amazonaws.com:8080/api",
  form, {
        headers : {
            Authorization : `Bearer ${token}`
        }
    });
    
    const filtered = res.data.filter((user) => user.email !== decoded.sub);

    return filtered;
}