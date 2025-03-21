import axios from "axios";
import { createContext, useContext, useEffect, useMemo, useState } from "react";

const AuthContext = createContext();

const AuthProvider = ({ children }) => {
  const [token, setToken_] = useState(localStorage.getItem("jwtToken"));

  const setToken = (newToken) => {
    setToken_(newToken);
  };

  useEffect(() => {
    const token = localStorage.getItem("jwtToken");
    if (token) {
      console.log("Token ustawiany w AuthProvider: ", token);
    }
  }, [token]);


  const contextValue = useMemo(
    () => ({
      token,
      setToken,
    }),
    [token]
  );

  return (
    <AuthContext.Provider value={contextValue}>{children}</AuthContext.Provider>
  );
};

export const useAuth = () => {
  return useContext(AuthContext);
};

export default AuthProvider;