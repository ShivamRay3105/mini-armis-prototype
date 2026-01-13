import {useState, useEffect} from 'react'
import axios from '../api/axios'
import useTokenRefresh from '../hooks/useTokenRefresh'

const Users = () => {
    const [users, setUsers] = useState([])
    const refreshToken = useTokenRefresh();

    useEffect(() => {
        let isMounted = true;
        const controller = new AbortController();

        const fetchUsers = async () => {
            try {
                const response = await axios.get('/user-service/users', {
                    signal: controller.signal
                });
                isMounted && setUsers(response.data);
            } catch (err) {
                console.error('Error fetching users:', err);
            }
        };
        fetchUsers();

        return () => {
            isMounted = false;
            controller.abort();
        };
    }, [])
  return (
    <div>
      <h2>Users List..</h2>
      {
        users?.length
        ? (
            <ul>
                {users.map((user,i) => <li key={i}>{user?.username}</li>)}
            </ul>
        ) : <p>No users to display</p>
      }
      <button onClick={() => refreshToken()}>Refresh</button>
      <br />
    </div>
  )
}

export default Users
